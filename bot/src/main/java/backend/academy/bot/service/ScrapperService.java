package backend.academy.bot.service;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.api.model.ListLinksResponse;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.api.model.TagsListResponse;
import backend.academy.api.model.TagsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class ScrapperService {
    @Autowired
    @Qualifier("scrapperRestClient")
    private RestClient client;

    public void deactivateTag(long chatId, String tag) {
        client.post()
                .uri("/tags/deactivate")
                .body(new TagsRequest(chatId, tag))
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .toBodilessEntity();
    }

    public void reactivateTag(long chatId, String tag) {
        client.post()
                .uri("/tags/reactivate")
                .body(new TagsRequest(chatId, tag))
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .toBodilessEntity();
    }

    public TagsListResponse getTagsList(long chatId) {
        return client.get()
                .uri("/tags/" + chatId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .body(TagsListResponse.class);
    }

    public LinkResponse addLink(long chatId, AddLinkRequest request) {
        return client.post()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .body(LinkResponse.class);
    }

    public void removeLink(long chatId, RemoveLinkRequest request) {
        client.method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .body(request)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .body(LinkResponse.class);
    }

    public void registerChar(long id) {
        client.post()
                .uri("/tg-chat/" + id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .toBodilessEntity();
    }

    public ListLinksResponse getTrackedLinks(long chatId) {
        return client.get()
                .uri("/links")
                .header("Tg-Chat-Id", String.valueOf(chatId))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, ScrapperService::handleErrorResponse)
                .body(ListLinksResponse.class);
    }

    private static void handleErrorResponse(HttpRequest request, ClientHttpResponse response) throws IOException {
        ApiErrorResponse details = null;
        try {
            details = new ObjectMapper().readValue(response.getBody(), ApiErrorResponse.class);
        } catch (JsonProcessingException ignored) {
        } catch (IOException exception) {
            log.atWarn().setMessage("IOException occurred").setCause(exception).log();
        }

        log.atWarn()
                .setMessage("Error response received")
                .addKeyValue("uri", request.getURI())
                .addKeyValue("method", request.getURI())
                .addKeyValue("code", response.getStatusCode())
                .addKeyValue("details", details)
                .log();

        throw new ApiErrorResponseException(details);
    }
}
