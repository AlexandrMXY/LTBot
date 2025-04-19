package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.web.clients.BotRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "http")
public class HttpBotNotificationSender implements BotNotificationSender {
    @Autowired
    private BotRestClient client;

    @Override
    public void sendUpdates(Updates updates) {
        if (updates == null) return;
        for (Update update : updates.getUpdates()) {
            var request = update.createRequest();

            client.postRequest("/updates", request);
        }
    }
}
