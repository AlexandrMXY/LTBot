package backend.academy.bot.telegram.command.session;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

public class SessionState {
    private final Map<String, Object> table = new HashMap<>();

    @Getter
    @Setter
    private SessionStateHandler stateHandler;

    @Getter
    private final long chatId;

    @Getter
    private final long initTime = System.currentTimeMillis();

    public SessionState(SessionStateHandler stateHandler, long chatId) {
        this.stateHandler = stateHandler;
        this.chatId = chatId;
    }

    public void setValue(String key, Object value) {
        table.put(key, value);
    }

    public <T> T getValue(String key) {
        return (T) table.get(key);
    }

    public synchronized boolean update(SessionEvent event) {
        return stateHandler.handle(this, event);
    }
}
