package backend.academy.bot.telegram.session;

import backend.academy.bot.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SessionContext {
    @Autowired
    private ScrapperService scrapperService;

    public ScrapperService scrapperService() {
        return scrapperService;
    }
}
