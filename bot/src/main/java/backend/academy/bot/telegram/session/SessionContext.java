package backend.academy.bot.telegram.session;

import backend.academy.bot.service.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    private final TelegramService telegramService;
//    @Value("app.url-regex")
    private String urlRegEx = "^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)$";

    @Autowired
    public SessionContext(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    public TelegramService telegramService() {
        return telegramService;
    }

    public String urlRegEx() {
        return urlRegEx;
    }
}
