package backend.academy.bot.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.telegram.TelegramService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        for (long id : ids) verify(telegramService).sendMessage(eq(id), anyString());
    }
}
