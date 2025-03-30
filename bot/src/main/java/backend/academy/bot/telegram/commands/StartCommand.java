package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
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
            try {
                scrapperService.registerChar(message.chat());
            } catch (ApiErrorResponseException e) {
                log.atWarn()
                        .setMessage("An error occurred during message processing")
                        .setCause(e)
                        .log();
            }
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
