package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.dto.updates.Updates;

public interface BotNotificationSender {
    void sendUpdates(Updates updates);

    void sendUpdatesWithoutFallback(Updates updates);
}
