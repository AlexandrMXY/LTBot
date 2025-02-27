package backend.academy.bot.service.telegram;

import backend.academy.bot.telegram.session.TelegramResponse;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** @noinspection LombokGetterMayBeUsed */
@Service
@Log4j2
public class TelegramService {
    @Autowired
    private TelegramBot bot;

    public void sendResponse(TelegramResponse response) {
        if (response == null) {
            return;
        }

        for (String message : response.messages()) {
            sendMessage(response.userId(), message);
        }
    }

    public void sendMessage(long chatId, String message) {
        SendMessage request = new SendMessage(chatId, message);
        var response = bot.execute(request);
        if (!response.isOk()) {
            log.error("Telegram sendMessage error: {}", response.message());
        }
    }

    public TelegramBot getBot() {
        return bot;
    }
}
