package backend.academy.bot.telegram.commands;

import backend.academy.bot.service.ScrapperService;
import backend.academy.bot.telegram.session.TelegramResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartCommand extends AbstractSimpleCommand {
    @Autowired
    private ScrapperService scrapperService;

    @PostConstruct
    private void init() {
        setProcessor((state, message, context) -> {
            scrapperService.registerChar(message.chat());
            return new TelegramResponse(message.chat(), "Welcome");
        });
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "start";
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
