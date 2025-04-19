package backend.academy.bot.service;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.LinkTagRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.requests.TagsRequest;
import backend.academy.api.model.responses.ApiErrorResponse;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.api.model.responses.TagsListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.function.Consumer;

@Service
@Slf4j
public class AsyncScrapperService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("scrapperWebClient")
    private WebClient client;

    public Mono<ResponseEntity<Void>> deactivateTag(long chatId, String tag) {
        return client.post()
            .uri("/tags/deactivate")
            .bodyValue(new TagsRequest(chatId, tag))
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .toBodilessEntity();
    }

    public Mono<ResponseEntity<Void>> reactivateTag(long chatId, String tag) {
        return client.post()
            .uri("/tags/reactivate")
            .bodyValue(new TagsRequest(chatId, tag))
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .toBodilessEntity();
    }

    public Mono<TagsListResponse> getTagsList(long chatId) {
        return client.get()
            .uri("/tags/" + chatId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .bodyToMono(TagsListResponse.class);
    }

    public Mono<LinkResponse> trackRequest(long chatId, AddLinkRequest request) {
        return client.post()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .bodyValue(request)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .bodyToMono(LinkResponse.class);
    }

    public Mono<LinkResponse> removeLink(long chatId, RemoveLinkRequest request) {
        return client.method(HttpMethod.DELETE)
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .bodyValue(request)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .bodyToMono(LinkResponse.class);
    }

    public Mono<ResponseEntity<Void>> registerChat(long id) {
        return client.post()
            .uri("/tg-chat/" + id)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .toBodilessEntity();
    }

    public Mono<ListLinksResponse> getTrackedLinks(long chatId) {
        return client.get()
            .uri("/links")
            .header("Tg-Chat-Id", String.valueOf(chatId))
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .bodyToMono(ListLinksResponse.class);
    }

    public Mono<ResponseEntity<Void>> addTagToLink(long charId, String link, String tag) {
        return client.post()
            .uri("/links/tags")
            .bodyValue(new LinkTagRequest(charId, tag, link))
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .toBodilessEntity();
    }

    public Mono<ResponseEntity<Void>> removeTagFromLink(long charId, String link, String tag) {
        return client.method(HttpMethod.DELETE)
            .uri("/links/tags")
            .bodyValue(new LinkTagRequest(charId, tag, link))
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .toBodilessEntity();
    }

    public Mono<ListLinksResponse> getLinksWithTag(long chatId, String tag) {
        return client.method(HttpMethod.GET)
            .uri("/tags/linksWithTag")
            .bodyValue(new TagsRequest(chatId, tag))
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .bodyToMono(ListLinksResponse.class);
    }

    public Mono<ResponseEntity<Void>> setNotificationPolicy(long chatId, NotificationPolicy policy) {
        return client.post()
            .uri("notifications/{user}/policy", Map.of("user", chatId))
            .bodyValue(policy)
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .toBodilessEntity();
    }

    public Mono<NotificationPolicy> getNotificationPolicy(long chatId) {
        return client.get()
            .uri("notifications/{user}/policy", Map.of("user", chatId))
            .retrieve()
            .onStatus(HttpStatusCode::isError, this::errorResponseHandler)
            .bodyToMono(NotificationPolicy.class);
    }

    private Mono<Throwable> errorResponseHandler(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
            .flatMap(response -> {
                ApiErrorResponse details = null;
                try {
                    details = objectMapper.readValue(response, ApiErrorResponse.class);
                } catch (Throwable t) {
                    return Mono.error(t);
                }
                return Mono.error(new ApiErrorResponseException(details));
            });
    }

}
