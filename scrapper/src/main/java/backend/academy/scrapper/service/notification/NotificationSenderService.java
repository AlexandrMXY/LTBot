package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.updates.Updates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationSenderService {
    private final BotNotificationSender primarySender;

    public NotificationSenderService(
            ScrapperConfig config,
            @Autowired(required = false) HttpBotNotificationSender httpSender,
            @Autowired(required = false) KafkaBotNotificationSender kafkaSender
    ) {
        primarySender = switch (config.messageTransport()) {
            case HTTP ->  httpSender;
            case KAFKA ->  kafkaSender;
        };
        if (primarySender == null) {
            throw new IllegalStateException("No primary notifications sender instance found");
        }
    }

    public void sendUpdates(Updates updates) {
        primarySender.sendUpdates(updates);
    }
}
