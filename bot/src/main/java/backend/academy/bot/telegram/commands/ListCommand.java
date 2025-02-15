package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ErrorResponseException;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.ListLinksResponse;
import backend.academy.bot.service.ScrapperService;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Log4j2
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
                context.telegramService().sendMessage(message.chat(), result.toString());
            } catch (ErrorResponseException exception) {
                ApiErrorResponse response = exception.details();
                if (response == null) {
                    log.warn("Invalid response: {}", "", exception);
                } else {
                    context.telegramService().sendMessage(message.chat(), response.exceptionMessage());
                }
            }
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
