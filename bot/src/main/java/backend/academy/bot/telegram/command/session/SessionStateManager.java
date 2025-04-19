package backend.academy.bot.telegram.command.session;

import backend.academy.bot.config.BotConfig;
import backend.academy.bot.telegram.command.Command;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@Slf4j
public class SessionStateManager {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Autowired
    private BotConfig botConfig;

    private Map<Long, SessionState> states = new HashMap<Long, SessionState>();

    public void onCommand(long chatId, Command command, MessageEvent event) {
        lock.writeLock().lock();
        try {
            states.put(chatId, command.initSession(chatId));
        } finally {
            lock.writeLock().unlock();
        }
        onUpdate(chatId, event);
    }


    public void onUpdate(long chatId, SessionEvent event) {
        boolean stillActive = true;

        lock.readLock().lock();
        try {
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
            stillActive = state.update(event);
        } finally {
            lock.readLock().unlock();
        }

        if (!stillActive) {
            lock.writeLock().lock();
            try {
                states.remove(chatId);
            } finally {
                lock.writeLock().unlock();
            }
        }
    }

    @Scheduled(fixedDelayString = "${app.session-timeout}")
    private void cleanup() {
        lock.writeLock().lock();
        try {
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
        } finally {
            lock.writeLock().unlock();
        }
    }
}
