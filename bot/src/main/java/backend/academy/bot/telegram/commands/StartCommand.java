package backend.academy.bot.telegram.commands;

import backend.academy.bot.service.ScrapperService;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class StartCommand extends AbstractSimpleCommand {
    @Autowired
    private ScrapperService scrapperService;

    @PostConstruct
    private void init() {
        setProcessor(((state, message, context) -> {
            scrapperService.registerChar(message.chat());
            return null;
        }));
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
