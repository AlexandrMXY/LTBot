package backend.academy.scrapper.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.LinkTagRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.requests.TagsRequest;
import backend.academy.scrapper.service.LinksService;
import backend.academy.scrapper.service.TagsService;
import backend.academy.scrapper.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@WebMvcTest
@EnableRetry
@Import({RateLimiterAutoConfiguration.class})
@ActiveProfiles({"noScheduling"})
public class ControllersRateLimiterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TagsService tagsService;

    @MockitoBean
    private LinksService linksService;

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;

    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    private static String json(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static Stream<Arguments> requests() {
        return Stream.of(
                Arguments.of(ChatController.RESILIENCE4J_INSTANCE_NAME, post("/tg-chat/444")),
                Arguments.of(ChatController.RESILIENCE4J_INSTANCE_NAME, delete("/tg-chat/444")),
                Arguments.of(
                        LinksController.RESILIENCE4J_INSTANCE_NAME,
                        get("/links").header("Tg-Chat-Id", 44)),
                Arguments.of(
                        LinksController.RESILIENCE4J_INSTANCE_NAME,
                        post("/links")
                                .header("Tg-Chat-Id", 44)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new AddLinkRequest(null, null, null)))),
                Arguments.of(
                        LinksController.RESILIENCE4J_INSTANCE_NAME,
                        delete("/links")
                                .header("Tg-Chat-Id", 44)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new RemoveLinkRequest(null)))),
                Arguments.of(
                        LinksController.RESILIENCE4J_INSTANCE_NAME,
                        delete("/links/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new LinkTagRequest(0, null, null)))),
                Arguments.of(
                        LinksController.RESILIENCE4J_INSTANCE_NAME,
                        post("/links/tags")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new LinkTagRequest(0, null, null)))),
                Arguments.of(NotificationsController.RESILIENCE4J_INSTANCE_NAME, get("/notifications/11/policy")),
                Arguments.of(
                        NotificationsController.RESILIENCE4J_INSTANCE_NAME,
                        post("/notifications/11/policy")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new NotificationPolicy(null, null)))),
                Arguments.of(
                        TagsController.RESILIENCE4J_INSTANCE_NAME,
                        post("/tags/deactivate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new TagsRequest(0, null)))),
                Arguments.of(
                        TagsController.RESILIENCE4J_INSTANCE_NAME,
                        post("/tags/reactivate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new TagsRequest(0, null)))),
                Arguments.of(TagsController.RESILIENCE4J_INSTANCE_NAME, get("/tags/111")),
                Arguments.of(
                        TagsController.RESILIENCE4J_INSTANCE_NAME,
                        get("/tags/linksWithTag")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json(new TagsRequest(0, null)))));
    }

    @ParameterizedTest
    @MethodSource("requests")
    public void endpoint_requestLimitExceeded_returns429(String limiter, RequestBuilder request) throws Exception {
        int RATE_LIMIT = 10;
        rateLimiterRegistry.rateLimiter(limiter).reservePermission(RATE_LIMIT);
        for (int i = 0; i < RATE_LIMIT; i++) {
            try {
                mockMvc.perform(request).andExpect((res) -> {});
            } catch (Exception e) {
            }
        }
        mockMvc.perform(request).andExpect(MockMvcResultMatchers.status().is(429));
    }
}
