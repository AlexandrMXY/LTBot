package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.ListLinksResponse;
import backend.academy.bot.service.ScrapperService;
import backend.academy.bot.telegram.session.TelegramResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ListCommand extends AbstractSimpleCommand {

    @Autowired
    private ScrapperService scrapperService;

    @PostConstruct
    private void init() {
        setProcessor((state, message, context) -> {
            try {
                ListLinksResponse links = scrapperService.getTrackedLinks(message.chat());
                StringBuilder result = new StringBuilder();
                for (LinkResponse link : links.links()) {
                    result.append(link.url()).append("\n");
                }
                if (links.links().isEmpty()) {
                    result.append("Тo tracked links");
                }
                return new TelegramResponse(message.chat(), result.toString());
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
        return "list";
    }

    @Override
    public String getDescription() {
        return "displays all tracked links";
    }
}
