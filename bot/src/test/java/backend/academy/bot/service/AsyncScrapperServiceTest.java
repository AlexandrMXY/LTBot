package backend.academy.bot.service;

import backend.academy.api.exceptions.ServerErrorErrorResponseException;
import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.requests.TagsRequest;
import backend.academy.bot.config.BotConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.restassured.specification.Argument;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ActiveProfiles({"noKafka"})
@Slf4j
@Execution(ExecutionMode.SAME_THREAD)
class AsyncScrapperServiceTest {
    @Autowired
    private AsyncScrapperService service;
    @Autowired
    private BotConfig config;

    private WireMockServer wireMockServer;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;

    @BeforeEach
    public void resetResilience4j() {
        circuitBreakerRegistry.circuitBreaker(AsyncScrapperService.RESILIENCE4J_INSTANCE_NAME).reset();
    }

    @BeforeEach
    public void initWiremock() {
        var portMatcher = Pattern.compile("(?:https?://)?localhost:(?<port>\\d{1,6})")
            .matcher(config.scrapperUrl());
        assertThat(portMatcher.matches())
            .withFailMessage("Invalid scrapper url for test config: localhost:<port> expected")
            .isTrue();
        int port = Integer.parseInt(portMatcher.group("port"));

        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(port));
        wireMockServer.start();
        configureFor("localhost", port);
        log.info("WireMock started on port {}", port);
    }


    @AfterEach
    public void shutdown() {
        resetAllScenarios();
        if (wireMockServer != null)
            wireMockServer.stop();
        log.info("WireMock stopped");
    }

    public Stream<Arguments> methods() {
        return Stream.of(
            Arguments.of(new MethodDescription(
                RequestMethod.POST,
                "/tags/deactivate",
                () -> service.deactivateTag(1L, ""))),
            Arguments.of(new MethodDescription(
                RequestMethod.POST,
                "/tags/reactivate",
                () -> service.reactivateTag(1L, ""))),
            Arguments.of(new MethodDescription(
                RequestMethod.GET,
                "/tags/1",
                () -> service.getTagsList(1L))),
            Arguments.of(new MethodDescription(
                RequestMethod.POST,
                "/links",
                () -> service.trackRequest(1L, new AddLinkRequest("", List.of(), List.of())))),
            Arguments.of(new MethodDescription(
                RequestMethod.DELETE,
                "/links",
                () -> service.removeLink(1L, new RemoveLinkRequest("")))),
            Arguments.of(new MethodDescription(
                RequestMethod.POST,
                "/tg-chat/1",
                () -> service.registerChat(1L))),
            Arguments.of(new MethodDescription(
                RequestMethod.GET,
                "/links",
                () -> service.getTrackedLinks(1L))),
            Arguments.of(new MethodDescription(
                RequestMethod.POST,
                "/links/tags",
                () -> service.addTagToLink(1L, "", ""))),
            Arguments.of(new MethodDescription(
                RequestMethod.DELETE,
                "/links/tags",
                () -> service.removeTagFromLink(1L, "", ""))),
            Arguments.of(new MethodDescription(
                RequestMethod.GET,
                "/tags/linksWithTag",
                () -> service.getLinksWithTag(1L, ""))),
            Arguments.of(new MethodDescription(
                RequestMethod.POST,
                "/notifications/1/policy",
                () -> service.setNotificationPolicy(1L, new NotificationPolicy(NotificationPolicy.INSTANT, 100)))),
            Arguments.of(new MethodDescription(
                RequestMethod.GET,
                "/notifications/1/policy",
                () -> service.getNotificationPolicy(1L)))
        );
    }


    @ParameterizedTest
    @MethodSource("methods")
    public void request_on200Ok_success(MethodDescription description) {
        stubFor(description.requestTargetMapping().willReturn(ok()));
        var response = description.requestCaller().get();
        response.block();
        verify(exactly(1), description.requestTargetPattern());
    }

    @ParameterizedTest
    @MethodSource("methods")
    public void request_on5xxErrorResponse_retry(MethodDescription description) {
        stubFor(description.requestTargetMapping().willReturn(serverError()));
        var response = description.requestCaller().get();
        response.doOnError((t) -> log.atWarn().setCause(t).log()).onErrorComplete().block();
        verify(exactly(3), description.requestTargetPattern());
    }

    @ParameterizedTest
    @MethodSource("methods")
    public void request_on4xxErrorResponse_noRetry(MethodDescription description) {
        stubFor(description.requestTargetMapping().willReturn(badRequest()));
        var response = description.requestCaller().get();
        response.onErrorComplete().block();
        verify(exactly(1), description.requestTargetPattern());
    }

    @ParameterizedTest
    @MethodSource("methods")
    public void request_on429ErrorResponse_retry(MethodDescription description) {
        stubFor(description.requestTargetMapping().willReturn(aResponse().withStatus(429)));
        var response = description.requestCaller().get();
        response.doOnError((t) -> log.atWarn().setCause(t).log()).onErrorComplete().block();
        verify(exactly(3), description.requestTargetPattern());
    }

    @ParameterizedTest
    @MethodSource("methods")
    public void request_waitLimitReached_error(MethodDescription description) {
        stubFor(description.requestTargetMapping().willReturn(ok().withFixedDelay(600)));
        assertThatThrownBy(() -> description.requestCaller().get().block())
            .hasCauseInstanceOf(TimeoutException.class);
    }

    @ParameterizedTest
    @MethodSource("methods")
    public void request_onMany5xxErrors_circuitBreakerOpen(MethodDescription description) {
        stubFor(description.requestTargetMapping().willReturn(serverError()));
        final int windowSize = 40;
        for (int i = 0; i < windowSize; i++) {
            description.requestCaller().get().onErrorComplete().block();
        }

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(AsyncScrapperService.RESILIENCE4J_INSTANCE_NAME);
        assertThat(cb.getState().name()).isEqualTo("OPEN");

        reset();
        stubFor(description.requestTargetMapping().willReturn(serverError().withFixedDelay(1000)));
        assertThatThrownBy(() -> description.requestCaller().get().block())
            .isInstanceOf(CallNotPermittedException.class);
    }


    public record MethodDescription(
        MappingBuilder requestTargetMapping,
        RequestPatternBuilder requestTargetPattern,
        Supplier<Mono<?>> requestCaller
    ) {
        MethodDescription(RequestMethod method, String urlRegex, Supplier<Mono<?>> caller) {
            this(request(String.valueOf(method), urlMatching(urlRegex)),
                requestedFor(String.valueOf(method), urlMatching(urlRegex)),
                caller);
        }
    }
}
