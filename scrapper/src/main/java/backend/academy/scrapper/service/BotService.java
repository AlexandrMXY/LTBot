package backend.academy.scrapper.service;

import backend.academy.api.model.LinkUpdate;
import backend.academy.scrapper.service.monitoring.Updates;
import backend.academy.scrapper.util.RequestErrorHandlers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class BotService {
    @Autowired
    @Qualifier("botRestClient")
    private RestClient client;

    public void sendUpdates(Updates updates) {
        if (updates == null) return;
        for (Updates.Update update : updates.getUpdates()) {
            var request = new LinkUpdate(0, update.url(), update.message(), update.users());

            client.post()
                    .uri("/updates")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
                    .toBodilessEntity();
        }
    }
}
