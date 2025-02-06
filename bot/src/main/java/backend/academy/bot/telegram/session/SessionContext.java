package backend.academy.bot.telegram.session;

import backend.academy.bot.service.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    private final TelegramService telegramService;
//    @Value("app.url-regex")
    private String urlRegEx = ".*";

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
