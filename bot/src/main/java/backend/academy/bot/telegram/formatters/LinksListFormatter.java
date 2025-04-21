package backend.academy.bot.telegram.formatters;

import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import org.springframework.stereotype.Component;

@Component
public class LinksListFormatter {
    public String format(ListLinksResponse listLinksResponse) {
        StringBuilder result = new StringBuilder();
        for (LinkResponse link : listLinksResponse.links()) {
            result.append(link.url());
            for (var tag : link.tags()) {
                result.append(" ").append(tag);
            }
            result.append("\n");
        }
        if (listLinksResponse.links().isEmpty()) {
            result.append("No tracked links");
        }
        return result.toString();
    }
}
