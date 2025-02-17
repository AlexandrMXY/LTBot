package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.bot.service.ScrapperService;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class UntrackCommand extends AbstractSimpleCommand {
    @Autowired
    private ScrapperService scrapperService;

    @PostConstruct
    private void init() {
        setProcessor(((state, message, context) -> {
            String[] msg = message.message().trim().split(" ");
            if (msg.length != 2) {
                context.telegramService().sendMessage(message.chat(), "Use /untrack <url>");
                return;
            }

            String url = msg[1];
            try {
                scrapperService.removeLink(message.chat(),
                    new RemoveLinkRequest(url));
                context.telegramService().sendMessage(message.chat(), "Success");
            } catch (ApiErrorResponseException exception) {
                ApiErrorResponse response = exception.details();
                if (response == null) {
                    log.warn("Invalid response: {}", "", exception);
                } else {
                    context.telegramService().sendMessage(message.chat(), response.exceptionMessage());
                }
            }
        }));
    }

    @Override
    public String getName() {
        return "untrack";
    }

    @Override
    public String getDescription() {
        return "stop tracking link";
    }
}
