package backend.academy.scrapper.web.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.util.RequestErrorHandlers;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BotRestClient {
    private final RestClient client;

    public BotRestClient(ScrapperConfig config) {
        this.client = RestClient.builder().baseUrl(config.botUrl()).build();
    }

    public <T> void postRequest(String url, T request) {
        client.post()
                .uri(url)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
                .toBodilessEntity();
    }
}
