package backend.academy.bot.telegram.session;

import backend.academy.bot.service.ScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    @Autowired
    private TelegramService telegramService;

    @Autowired
    private ScrapperService scrapperService;

    //    @Value("app.url-regex")
    private String urlRegEx =
            "^https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)$";

    public TelegramService telegramService() {
        return telegramService;
    }

    public ScrapperService scrapperService() {
        return scrapperService;
    }

    public String urlRegEx() {
        return urlRegEx;
    }
}
