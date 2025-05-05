package backend.academy.bot.service;

import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.api.model.responses.TagsListResponse;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface ScrapperService {
    Mono<ResponseEntity<Void>> deactivateTag(long chatId, String tag);

    Mono<ResponseEntity<Void>> reactivateTag(long chatId, String tag);

    Mono<TagsListResponse> getTagsList(long chatId);

    Mono<LinkResponse> trackRequest(long chatId, AddLinkRequest request);

    Mono<LinkResponse> removeLink(long chatId, RemoveLinkRequest request);

    Mono<ResponseEntity<Void>> registerChat(long id);

    Mono<ListLinksResponse> getTrackedLinks(long chatId);

    Mono<ResponseEntity<Void>> addTagToLink(long charId, String link, String tag);

    Mono<ResponseEntity<Void>> removeTagFromLink(long charId, String link, String tag);

    Mono<ListLinksResponse> getLinksWithTag(long chatId, String tag);

    Mono<ResponseEntity<Void>> setNotificationPolicy(long chatId, NotificationPolicy policy);

    Mono<NotificationPolicy> getNotificationPolicy(long chatId);
}
