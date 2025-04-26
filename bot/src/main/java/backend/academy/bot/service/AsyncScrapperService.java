package backend.academy.bot.service;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.exceptions.BadRequestErrorResponseException;
import backend.academy.api.exceptions.ServerErrorErrorResponseException;
import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.LinkTagRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.requests.TagsRequest;
import backend.academy.api.model.responses.ApiErrorResponse;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.api.model.responses.TagsListResponse;
import backend.academy.bot.config.BotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@Retry(name = AsyncScrapperService.RESILIENCE4J_INSTANCE_NAME)
@CircuitBreaker(name = AsyncScrapperService.RESILIENCE4J_INSTANCE_NAME)
@RateLimiter(name = AsyncScrapperService.RESILIENCE4J_INSTANCE_NAME)
@TimeLimiter(name = AsyncScrapperService.RESILIENCE4J_INSTANCE_NAME)
public class AsyncScrapperService {
    public static final String RESILIENCE4J_INSTANCE_NAME = "scrapperService";

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
        return clientResponse.toEntity(byte[].class).flatMap(response -> {
            log.atDebug()
                .addKeyValue("status", clientResponse.statusCode())
                .addKeyValue("uri", clientResponse.request().getURI())
                .addKeyValue("method", clientResponse.request().getMethod())
                .log("Error response from server received: {}", response);
            ApiErrorResponse details = null;
            try {
                details = objectMapper.readValue(response.getBody(), ApiErrorResponse.class);
            } catch (Throwable t) {
                log.atWarn()
                    .setMessage("Error during error response details parsing")
                    .setCause(t)
                    .log();
            }

            if (response.getStatusCode().is5xxServerError())
                return Mono.error(new ServerErrorErrorResponseException(
                    details,
                    response.getStatusCode().value()));
            if (response.getStatusCode().is4xxClientError())
                return Mono.error(new BadRequestErrorResponseException(
                    details,
                    response.getStatusCode().value()));

            return Mono.error(new ApiErrorResponseException(
                details,
                response.getStatusCode().value()));
        });
    }
}
