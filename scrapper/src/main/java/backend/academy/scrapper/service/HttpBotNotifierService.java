package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.web.clients.BotRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class HttpBotNotifierService implements BotNotifierService {
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
