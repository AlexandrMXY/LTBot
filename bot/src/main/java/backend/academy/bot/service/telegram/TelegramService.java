package backend.academy.bot.service.telegram;

import backend.academy.bot.telegram.session.TelegramResponse;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/** @noinspection LombokGetterMayBeUsed */
@Service
@Slf4j
public class TelegramService {
    @Autowired
    private TelegramBot bot;

    public void sendMessage(long chatId, String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        SendMessage request = new SendMessage(chatId, message);
        var response = bot.execute(request);
        if (!response.isOk()) {
            log.atWarn()
                    .setMessage("Telegram sendMessage error")
                    .addKeyValue("code", response.errorCode())
                    .addKeyValue("description", response.description())
                    .addKeyValue("parameters", response.parameters())
                    .log();
        }
    }

    public TelegramBot getBot() {
        return bot;
    }
}
