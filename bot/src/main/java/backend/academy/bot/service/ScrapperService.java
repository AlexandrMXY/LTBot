package backend.academy.bot.service;

import backend.academy.api.exceptions.ErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.bot.BotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;

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

    public LinkResponse addLinks(long chatId, AddLinkRequest request) {
        return client.post()
            .uri(baseUrl + "/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .body(request)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request_, rawErrorResponse) -> {
                ApiErrorResponse errorResponse =
                    new ObjectMapper().readValue(rawErrorResponse.getBody(), ApiErrorResponse.class);
                throw new ErrorResponseException(errorResponse.description());
            })
            .body(LinkResponse.class);
    }

    public void registerChar(long id) {
        var response = client.post()
            .uri(baseUrl + "/tg-chat/" + id)
            .retrieve()
            .onStatus(HttpStatusCode::isError, (request_, rawErrorResponse) ->
                log.error("Error registering user: {}", rawErrorResponse.getStatusCode()));
    }
}
