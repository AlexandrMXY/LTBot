package backend.academy.bot;

import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.api.model.responses.TagsListResponse;
import backend.academy.bot.config.RedisConfig;
import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.ScrapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Primary
@Profile("!noRedis")
public class CacheScrapperService implements ScrapperService {
    static final String USER_CACHE_PREFIX = "user:";

    @Autowired
    AsyncScrapperService asyncScrapperService;

    @Autowired
    @Qualifier(RedisConfig.LINKS_TEMPLATE)
    private RedisTemplate<String, ListLinksResponse> template;

    @Override
    public Mono<ResponseEntity<Void>> removeTagFromLink(long chatId, String link, String tag) {
        template.delete(USER_CACHE_PREFIX + chatId);
        return asyncScrapperService.removeTagFromLink(chatId, link, tag);
    }

    @Override
    public Mono<ResponseEntity<Void>> addTagToLink(long chatId, String link, String tag) {
        template.delete(USER_CACHE_PREFIX + chatId);
        return asyncScrapperService.addTagToLink(chatId, link, tag);
    }

    @Override
    public Mono<LinkResponse> removeLink(long chatId, RemoveLinkRequest request) {
        template.delete(USER_CACHE_PREFIX + chatId);
        return asyncScrapperService.removeLink(chatId, request);
    }

    @Override
    public Mono<LinkResponse> trackRequest(long chatId, AddLinkRequest request) {
        template.delete(USER_CACHE_PREFIX + chatId);
        return asyncScrapperService.trackRequest(chatId, request);
    }

    @Override
    public Mono<ListLinksResponse> getTrackedLinks(long chatId) {
        String cacheKey = USER_CACHE_PREFIX + chatId;
        ListLinksResponse result = template.opsForValue().get(cacheKey);
        if (result != null) {
            return Mono.just(result);
        }
        template.delete(USER_CACHE_PREFIX + chatId);
        return asyncScrapperService.getTrackedLinks(chatId).doOnNext(response -> template.opsForValue()
                .set(cacheKey, response));
    }

    @Override
    public Mono<ResponseEntity<Void>> deactivateTag(long chatId, String tag) {
        return asyncScrapperService.deactivateTag(chatId, tag);
    }

    @Override
    public Mono<ResponseEntity<Void>> reactivateTag(long chatId, String tag) {
        return asyncScrapperService.reactivateTag(chatId, tag);
    }

    @Override
    public Mono<TagsListResponse> getTagsList(long chatId) {
        return asyncScrapperService.getTagsList(chatId);
    }

    @Override
    public Mono<ResponseEntity<Void>> registerChat(long id) {
        return asyncScrapperService.registerChat(id);
    }

    @Override
    public Mono<ListLinksResponse> getLinksWithTag(long chatId, String tag) {
        return asyncScrapperService.getLinksWithTag(chatId, tag);
    }

    @Override
    public Mono<ResponseEntity<Void>> setNotificationPolicy(long chatId, NotificationPolicy policy) {
        return asyncScrapperService.setNotificationPolicy(chatId, policy);
    }

    @Override
    public Mono<NotificationPolicy> getNotificationPolicy(long chatId) {
        return asyncScrapperService.getNotificationPolicy(chatId);
    }
}
