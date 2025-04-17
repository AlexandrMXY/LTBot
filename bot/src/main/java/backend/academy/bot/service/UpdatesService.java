package backend.academy.bot.service;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.formatters.UpdateFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdatesService {
    @Autowired
    private TelegramService telegramService;

    @Autowired
    private UpdateFormatter formatter;

    public void processUpdate(LinkUpdate update) {
        telegramService.sendMessage(update.chatId(), formatter.format(update));
    }
}
