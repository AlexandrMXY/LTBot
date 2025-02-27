package backend.academy.bot.telegram.session;

import backend.academy.bot.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    @Autowired
    private ScrapperService scrapperService;

    @Value("${app.url-regex}")
    private String urlRegEx;

    public ScrapperService scrapperService() {
        return scrapperService;
    }

    public String urlRegEx() {
        return urlRegEx;
    }
}
