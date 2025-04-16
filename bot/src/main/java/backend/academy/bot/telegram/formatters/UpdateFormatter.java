package backend.academy.bot.telegram.formatters;

import backend.academy.api.model.LinkUpdate;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class UpdateFormatter {
    public String format(LinkUpdate update) {
        return String.format(
                "New %s from user %s at %s at %s%n%s",
                getUpdateTypeName(update.type()),
                update.author(),
                update.url(),
                convertTime(update.time()),
                update.content());
    }

    private String getUpdateTypeName(String updateType) {
        return switch (updateType) {
            case LinkUpdate.Types.COMMENT -> "comment";
            case LinkUpdate.Types.ANSWER -> "answer";
            case LinkUpdate.Types.ISSUE -> "issue";
            case LinkUpdate.Types.PULL_REQUEST -> "pull request";
            case null -> "update";
            default -> "update";
        };
    }

    private String convertTime(long time) {
        return Instant.ofEpochSecond(time).toString();
    }
}
