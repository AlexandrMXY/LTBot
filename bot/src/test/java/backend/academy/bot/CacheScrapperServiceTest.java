package backend.academy.bot;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.bot.config.BotConfig;
import backend.academy.bot.config.RedisConfig;
import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.ScrapperService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;

@SpringBootTest(
        classes = {
            RedisConfig.class,
            CacheScrapperService.class,
            AsyncScrapperService.class,
            WebClient.class,
            BotApplication.class
        })
@EnableConfigurationProperties(BotConfig.class)
@EnableCaching
@Testcontainers
@ActiveProfiles("noKafka")
public class CacheScrapperServiceTest {
    @ServiceConnection
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:latest").withExposedPorts(6379);

    @Autowired
    private ScrapperService scrapperService;

    @MockitoBean
    private AsyncScrapperService asyncScrapperService;

    @Autowired
    @Qualifier(RedisConfig.LINKS_TEMPLATE)
    private RedisTemplate<String, ListLinksResponse> template;

    @BeforeEach
    public void reset() {
        Mockito.reset(asyncScrapperService);
    }

    @Test
    public void getLinks_noCache_returnRequestedValue() {
        ListLinksResponse linksResponse = new ListLinksResponse(List.of(
                new LinkResponse(11, "qwerti", List.of("a", "b"), List.of("q", "qq")),
                new LinkResponse(12, "qwerty", List.of("c", "d"), List.of("qqq", "qqqq"))));
        template.delete(CacheScrapperService.USER_CACHE_PREFIX + 111);
        doReturn(Mono.just(linksResponse)).when(asyncScrapperService).getTrackedLinks(eq(111L));

        assertThat(scrapperService.getTrackedLinks(111).block()).isEqualTo(linksResponse);
        assertThat(mockingDetails(asyncScrapperService).getInvocations().size()).isEqualTo(1);
    }

    @Test
    public void getLinks_updated_returnCorrectValue() {
        ListLinksResponse linksResponse = new ListLinksResponse(List.of(
                new LinkResponse(11, "qwerti", List.of("a", "b"), List.of("q", "qq")),
                new LinkResponse(12, "qwerty", List.of("c", "d"), List.of("qqq", "qqqq"))));
        template.opsForValue().set(CacheScrapperService.USER_CACHE_PREFIX + 111, linksResponse);
        Mockito.verifyNoInteractions(asyncScrapperService);
        assertThat(scrapperService.getTrackedLinks(111).block()).isEqualTo(linksResponse);
    }

    @Test
    public void getLinks_noCache_correctlyUpdateCache() {
        template.delete(CacheScrapperService.USER_CACHE_PREFIX + 111);
        ListLinksResponse linksResponse = new ListLinksResponse(List.of(
                new LinkResponse(11, "qwerti", List.of("a", "b"), List.of("q", "qq")),
                new LinkResponse(12, "qwerty", List.of("c", "d"), List.of("qqq", "qqqq"))));
        doReturn(Mono.just(linksResponse)).when(asyncScrapperService).getTrackedLinks(eq(111L));
        assertThat(scrapperService.getTrackedLinks(111).block()).isEqualTo(linksResponse);
        verify(asyncScrapperService, times(1)).getTrackedLinks(eq(111L));

        assertThat(template.opsForValue().get(CacheScrapperService.USER_CACHE_PREFIX + 111))
                .isEqualTo(linksResponse);
    }

    @Test
    public void addLinks_hasCache_invalidateCache() {
        ListLinksResponse linksResponse = new ListLinksResponse(List.of(
                new LinkResponse(11, "qwerti", List.of("a", "b"), List.of("q", "qq")),
                new LinkResponse(12, "qwerty", List.of("c", "d"), List.of("qqq", "qqqq"))));
        template.opsForValue().set(CacheScrapperService.USER_CACHE_PREFIX + 111, linksResponse);
        scrapperService.trackRequest(111, new AddLinkRequest("", List.of("a", "b"), List.of("q", "qq")));

        assertThat(template.hasKey(CacheScrapperService.USER_CACHE_PREFIX + 111))
                .isFalse();
    }
}
