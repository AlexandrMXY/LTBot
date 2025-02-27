package backend.academy.bot.controllers;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.telegram.TelegramService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class UpdatesControllerTest {
    @Mock
    TelegramService telegramService;
    @InjectMocks
    UpdatesController controller;

    @Test
    void updates_requestReceived_sendMessagesToAllUsers() {
        List<Long> ids = List.of(0L, 1L, 2L, 444L);
        var request = new LinkUpdate(0, "url", "desc", ids);

        controller.updates(request);

        verify(telegramService, times(ids.size())).sendMessage(longThat(ids::contains), anyString());
        for (long id : ids)
            verify(telegramService).sendMessage(eq(id), anyString());
    }
}
