package backend.academy.bot.telegram.formatters;

import backend.academy.api.model.LinkUpdate;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Component
public class UpdateFormatter {
    public String format(LinkUpdate update) {
        return String.format("New update at %s from user %s at %s\n%s",
            update.url(),
            update.author(),
            convertTime(update.time()),
            update.content());
    }

    private String convertTime(long time) {
        return Instant.ofEpochSecond(time).toString();
    }
}
