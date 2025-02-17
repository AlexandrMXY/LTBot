package backend.academy.bot.service;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.ListLinksResponse;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.bot.BotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Log4j2
public class ScrapperService {
    private final RestClient client;
    private final String baseUrl;

    @Autowired
    public ScrapperService(BotConfig config) {
        client = RestClient.builder()
            .build();

        baseUrl = config.scrapperUrl();
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        return client.post()
            .uri(baseUrl + "/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .body(request)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request_, rawErrorResponse) -> {
                ApiErrorResponse errorResponse =
                    new ObjectMapper().readValue(rawErrorResponse.getBody(), ApiErrorResponse.class);
                throw new ApiErrorResponseException(errorResponse);
            })
            .body(LinkResponse.class);
    }

    public LinkResponse removeLink(long chatId, RemoveLinkRequest request) {
        return client.method(HttpMethod.DELETE)
            .uri(baseUrl + "/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .body(request)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request_, rawErrorResponse) -> {
                ApiErrorResponse errorResponse =
                    new ObjectMapper().readValue(rawErrorResponse.getBody(), ApiErrorResponse.class);
                throw new ApiErrorResponseException(errorResponse);
            })
            .body(LinkResponse.class);
    }

    public void registerChar(long id) {
        client.post()
            .uri(baseUrl + "/tg-chat/" + id)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request_, rawErrorResponse) ->
                log.error("Error registering user: {}", rawErrorResponse.getStatusCode()));
    }

    public ListLinksResponse getTrackedLinks(long chatId) {
        return client.get()
            .uri(baseUrl + "/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request_, rawErrorResponse) -> {
                ApiErrorResponse errorResponse =
                    new ObjectMapper().readValue(rawErrorResponse.getBody(), ApiErrorResponse.class);
                throw new ApiErrorResponseException(errorResponse);
            })
            .body(ListLinksResponse.class);
    }
}
