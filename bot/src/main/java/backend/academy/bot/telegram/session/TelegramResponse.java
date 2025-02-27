package backend.academy.bot.telegram.session;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
public class TelegramResponse {
    private long userId;
    private List<String> messages;

    public TelegramResponse(long userId, String message) {
        this.userId = userId;
        this.messages = new ArrayList<>();
        if (message != null) {
            this.messages.add(message);
        }
    }

    public void addMessage(String message) {
        if (message != null) {
            messages.add(message);
        }
    }
}
