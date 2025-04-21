package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.configuration.RedisConfig;
import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.util.TimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final String TIME_CACHE_PREFIX = "time:";

    @Autowired
    private BotNotificationSender botNotificationSender;

    @Autowired
    @Qualifier(RedisConfig.UPDATES_TEMPLATE)
    public RedisTemplate<String, Update> template;

    private int lastTimeSendingDelayedUpdates = TimeUtils.getMinuteOfDay();

    public void sendUpdates(Updates updates) {
        Updates delayedUpdates = updates.extractDelayed();
        botNotificationSender.sendUpdates(updates);
        processDelayedUpdates(delayedUpdates);
    }

    private void processDelayedUpdates(Updates updates) {
        for (Update update : updates.getUpdates()) {
            template.opsForList().rightPush(TIME_CACHE_PREFIX + update.notificationTime(), update);
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void sendDelayedUpdates() {
        int nowMinute = TimeUtils.getMinuteOfDay();
        for (int i = lastTimeSendingDelayedUpdates; i <= nowMinute; i++) {
            sendDelayedUpdates(i);
        }
        lastTimeSendingDelayedUpdates = nowMinute;
    }

    private void sendDelayedUpdates(int time) {
        String key = TIME_CACHE_PREFIX + time;
        Updates updates = new Updates(template.opsForList().range(key, 0, -1));
        botNotificationSender.sendUpdates(updates);
        template.delete(key);
    }
}
