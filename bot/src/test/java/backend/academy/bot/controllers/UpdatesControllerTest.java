package backend.academy.bot.controllers;

import static org.mockito.Mockito.*;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.telegram.TelegramService;
import java.util.List;
import backend.academy.bot.telegram.formatters.UpdateFormatter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdatesControllerTest {
    @Mock
    TelegramService telegramService;
    @Spy
    UpdateFormatter formatter = new UpdateFormatter();

    @InjectMocks
    UpdatesController controller;


    @Test
    void updates_requestReceived_sendMessagesToCorrectUser() {
        var request = new LinkUpdate(0, 1000, "A", "B", "C");
        controller.updates(request);
        verify(telegramService).sendMessage(eq(0L), anyString());
    }
}
