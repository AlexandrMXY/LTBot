package backend.academy.scrapper.service;

import backend.academy.scrapper.service.monitoring.Updates;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class BotService {
    private RestClient client;

    public BotService(@Value("app.bot-url") String botUrl) {
        client = RestClient.builder()
            .baseUrl(botUrl)
            .build();
    }

    public void sendUpdates(Updates updates) {
        for (Updates.Update update : updates.getUpdates()) {
//            update.
        }
    }
}
