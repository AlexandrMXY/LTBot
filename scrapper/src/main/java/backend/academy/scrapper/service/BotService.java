package backend.academy.scrapper.service;

import backend.academy.api.model.LinkUpdate;
import backend.academy.scrapper.service.monitoring.Updates;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.client.RestClient;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
@Log4j2
public class BotService {
    private final RestClient client;

    public BotService(@Value("${app.bot-url}") String botUrl) {
        client = RestClient.builder()
            .baseUrl(botUrl)
            .build();
        log.info("Bot service initialized with target url {}", botUrl);
    }

    public void sendUpdates(Updates updates) {
        if (updates == null)
            return;
        for (Updates.Update update : updates.getUpdates()) {
            var request = new LinkUpdate(0, update.url(), update.message(), update.users());
            log.info("Sending update: {}", request);
            var response = client.post()
                .uri("/updates")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    log.error("Error request: {} -> {}",
                        req.getURI().toString(),
                        new BufferedReader(
                            new InputStreamReader(resp.getBody())).lines().collect(Collectors.joining()));
                    throw new ErrorResponseException(resp.getStatusCode());
                })
                .toBodilessEntity();
            log.info("Response: {}", response.toString());
        }
    }
}
