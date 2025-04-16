package backend.academy.bot.moduleTests;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.service.ScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.service.telegram.UserSessionManagementService;
import backend.academy.bot.telegram.session.TelegramResponse;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
public class TelegramCommandsTest {

    @MockitoSpyBean
    private TelegramService telegramService;

    @MockitoBean
    private ScrapperService scrapperService;

    @Autowired
    private UserSessionManagementService userSessionManagementService;

    @BeforeEach
    public void init() {
        doNothing().when(telegramService).sendMessage(anyLong(), any());
        doNothing().when(telegramService).sendResponse(any());
    }

    @Test
    public void processMessage_unknownCommand_answerUnknownCommandMessage() {
        userSessionManagementService.processMessage(new MessageDto(0, "/qwertyuiop["));

        ArgumentCaptor<TelegramResponse> responseCaptor = ArgumentCaptor.forClass(TelegramResponse.class);
        verify(telegramService).sendResponse(responseCaptor.capture());
        TelegramResponse response = responseCaptor.getValue();

        assertEquals(1, response.messages().size());
        assertEquals("Unknown command", response.messages().getFirst());
        assertEquals(0, response.userId());
    }

    @Test
    public void processMessage_listCommand_correctFormat() {
        when(scrapperService.getTrackedLinks(eq(0L)))
                .thenReturn(new ListLinksResponse(List.of(
                        new LinkResponse(
                                0, "https://github.com/ESCOMP/atmospheric_physics", List.of("A"), List.of("A")),
                        new LinkResponse(0, "https://github.com/topics/3b-yp-1tri-2025", List.of("B"), List.of("A")),
                        new LinkResponse(0, "https://github.com/frappe/erpnext", List.of("B"), List.of("B")),
                        new LinkResponse(
                                0,
                                "https://stackoverflow.com/questions/14938748/how-to-lazy-load-collection-when-using-spring-data-jpa-with-hibernate-from-an",
                                List.of("A"),
                                List.of("A")))));

        userSessionManagementService.processMessage(new MessageDto(0, "/list"));

        ArgumentCaptor<TelegramResponse> responseCaptor = ArgumentCaptor.forClass(TelegramResponse.class);
        verify(telegramService).sendResponse(responseCaptor.capture());
        TelegramResponse response = responseCaptor.getValue();

        assertEquals(0, response.userId());
        assertEquals(1, response.messages().size());

        assertThatCharSequence(response.messages().getFirst())
                .isEqualTo(
                        """
                https://github.com/ESCOMP/atmospheric_physics A
                https://github.com/topics/3b-yp-1tri-2025 B
                https://github.com/frappe/erpnext B
                https://stackoverflow.com/questions/14938748/how-to-lazy-load-collection-when-using-spring-data-jpa-with-hibernate-from-an A
                """);
    }

    @Test
    public void processMessage_listCommand_noLinks_correctFormat() {
        when(scrapperService.getTrackedLinks(eq(0L))).thenReturn(new ListLinksResponse(List.of()));

        userSessionManagementService.processMessage(new MessageDto(0, "/list"));

        ArgumentCaptor<TelegramResponse> responseCaptor = ArgumentCaptor.forClass(TelegramResponse.class);
        verify(telegramService).sendResponse(responseCaptor.capture());
        TelegramResponse response = responseCaptor.getValue();

        assertEquals(0, response.userId());
        assertEquals(1, response.messages().size());

        assertThatCharSequence(response.messages().getFirst()).isEqualTo("No tracked links");
    }
}
