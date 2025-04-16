package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.responses.ApiErrorResponse;
import backend.academy.bot.service.ScrapperService;
import backend.academy.bot.telegram.session.TelegramResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommand extends AbstractSimpleCommand {
    @Autowired
    private ScrapperService scrapperService;

    @PostConstruct
    private void init() {
        setProcessor((state, message, context) -> {
            String[] msg = message.message().trim().split(" ");
            if (msg.length != 2) {
                return new TelegramResponse(message.chat(), "Use /untrack <url>");
            }

            String url = msg[1];
            try {
                scrapperService.removeLink(message.chat(), new RemoveLinkRequest(url));
                return new TelegramResponse(message.chat(), "Success");
            } catch (ApiErrorResponseException exception) {
                ApiErrorResponse response = exception.details();
                if (response != null) {
                    return new TelegramResponse(message.chat(), response.exceptionMessage());
                }
            }
            return null;
        });
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
