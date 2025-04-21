package backend.academy.bot.telegram.command.session;

import backend.academy.bot.config.BotConfig;
import backend.academy.bot.telegram.command.Command;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SessionStateManager {
    @Autowired
    private BotConfig botConfig;

    private Map<Long, SessionState> states = new HashMap<Long, SessionState>();

    public synchronized void onCommand(long chatId, Command command, MessageEvent event) {
        states.put(chatId, command.initSession(chatId));
        onUpdate(chatId, event);
    }

    public synchronized void onUpdate(long chatId, SessionEvent event) {
        log.atDebug()
                .setMessage("Updating session state for chat id " + chatId)
                .addKeyValue("chatId", chatId)
                .addKeyValue("event", event)
                .log();
        if (!states.containsKey(chatId)) {
            log.atWarn()
                    .setMessage("Unexpected event: session not found")
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("event", event)
                    .log();
            return;
        }

        SessionState state = states.get(chatId);
        boolean stillActive = state.update(event);

        if (!stillActive) {
            states.remove(chatId);
        }
    }

    @Scheduled(fixedDelayString = "${app.session-timeout}")
    public synchronized void cleanup() {
        long cleanupTime = System.currentTimeMillis() - botConfig.sessionTimeout();
        List<Long> toRemove = new ArrayList<Long>();
        for (SessionState state : states.values()) {
            if (state.initTime() < cleanupTime) {
                toRemove.add(state.chatId());
            }
        }
        for (Long chatId : toRemove) {
            states.remove(chatId);
        }
    }
}
