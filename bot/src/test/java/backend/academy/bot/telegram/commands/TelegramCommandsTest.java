package backend.academy.bot.telegram.commands;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.bot.config.BotConfig;
import backend.academy.bot.config.BotSpringConfig;
import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.MessageProcessorService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.session.TelegramResponse;
import java.util.List;
import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
public class TelegramCommandsTest {
    @MockitoSpyBean
    private TelegramService telegramService;
    @MockitoBean
    private AsyncScrapperService scrapperService;
    @Autowired
    private SessionStateManager sessionStateManager;
    @Autowired
    private MessageProcessorService messageProcessorService;

    @BeforeEach
    public void init() {
        doNothing().when(telegramService).sendMessage(anyLong(), any());
    }

    @Test
    public void processMessage_listCommand_correctFormat() {
        when(scrapperService.getTrackedLinks(eq(0L)))
                .thenReturn(Mono.just(new ListLinksResponse(List.of(
                        new LinkResponse(
                                0, "https://github.com/ESCOMP/atmospheric_physics", List.of("A"), List.of("A")),
                        new LinkResponse(0, "https://github.com/topics/3b-yp-1tri-2025", List.of("B"), List.of("A")),
                        new LinkResponse(0, "https://github.com/frappe/erpnext", List.of("B"), List.of("B")),
                        new LinkResponse(
                                0,
                                "https://stackoverflow.com/questions/14938748/how-to-lazy-load-collection-when-using-spring-data-jpa-with-hibernate-from-an",
                                List.of("A"),
                                List.of("A"))))));

        sendMessageToSessionStateManager(new MessageDto(0, "/list"));

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramService).sendMessage(eq(0L), responseCaptor.capture());
        String response = responseCaptor.getValue();

        assertThatCharSequence(response) .isEqualTo("""
                https://github.com/ESCOMP/atmospheric_physics A
                https://github.com/topics/3b-yp-1tri-2025 B
                https://github.com/frappe/erpnext B
                https://stackoverflow.com/questions/14938748/how-to-lazy-load-collection-when-using-spring-data-jpa-with-hibernate-from-an A
                """);
    }

    @Test
    public void processMessage_listCommand_noLinks_correctFormat() {
        when(scrapperService.getTrackedLinks(eq(0L))).thenReturn(Mono.just(new ListLinksResponse(List.of())));

        sendMessageToSessionStateManager(new MessageDto(0, "/list"));

        ArgumentCaptor<String> responseCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramService).sendMessage(eq(0L), responseCaptor.capture());
        String response = responseCaptor.getValue();

        assertThatCharSequence(response).isEqualTo("No tracked links");
    }

    private void sendMessageToSessionStateManager(MessageDto messageDto) {
//        sessionStateManager.onUpdate(messageDto.chat(), new MessageEvent(messageDto));
        messageProcessorService.processMessage(messageDto);
    }
}
