package backend.academy.bot.service.telegram;

import backend.academy.bot.BotConfig;
import backend.academy.bot.telegram.session.TelegramResponse;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TelegramService {
    private static final Logger LOGGER = LogManager.getLogger();

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

    public SendResponse sendMessage(long chatId, String message) {
        SendMessage request = new SendMessage(chatId, message);
        return bot.execute(request);
    }

    public TelegramBot getBot() {
        return bot;
    }
}
