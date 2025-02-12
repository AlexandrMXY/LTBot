package backend.academy.bot.service;

import backend.academy.api.exceptions.ErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.bot.BotConfig;
import backend.academy.bot.telegram.session.SessionContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.List;

@Service
public class ScrapperService {
    private final RestClient client;
    private final String baseUrl;

    @Autowired
    public ScrapperService(BotConfig config) {
        client = RestClient.builder()
//            .baseUrl(config.scrapperUrl())
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
}
