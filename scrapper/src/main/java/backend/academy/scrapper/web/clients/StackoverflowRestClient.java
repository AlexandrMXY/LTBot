package backend.academy.scrapper.web.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.model.stackoverflow.CommentsResponse;
import backend.academy.scrapper.util.MapBuilder;
import backend.academy.scrapper.util.RequestErrorHandlers;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.Map;

@Component
public class StackoverflowRestClient {
    private final RestClient client;

    public StackoverflowRestClient(ScrapperConfig config) {
        this.client = RestClient.builder()
            .baseUrl(config.stackoverflowApiUrl())
            .defaultUriVariables(MapBuilder.<String, String>builder()
                .put("key", config.stackOverflow().key())
                .put("access_token", config.stackOverflow().accessToken())
                .put("site", "stackoverflow")
                .build())
            .build();
    }

    public <T> T getRequest(String uri, Class<T> responseType) {
        return client.get()
            .uri(uri)
            .retrieve()
            .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
            .toEntity(responseType)
            .getBody();
    }
}
