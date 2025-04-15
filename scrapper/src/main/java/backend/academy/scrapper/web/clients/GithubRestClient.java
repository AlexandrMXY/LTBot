package backend.academy.scrapper.web.clients;

import backend.academy.scrapper.ScrapperConfig;
import backend.academy.scrapper.util.RequestErrorHandlers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GithubRestClient {
    private final RestClient client;

    @Autowired
    public GithubRestClient(ScrapperConfig config) {
        this.client = RestClient.builder()
            .baseUrl(config.githubApiUrl())
            .defaultHeader("Authorization", "Bearer " + config.githubToken())
            .build();
    }

    public <T> ResponseEntity<T[]> getRequestForArray(String url, Class<T[]> responseType) {
        return client.get()
            .uri(url)
            .retrieve()
            .onStatus(HttpStatusCode::isError, RequestErrorHandlers::logAndThrow)
            .toEntity(responseType);
    }
}
