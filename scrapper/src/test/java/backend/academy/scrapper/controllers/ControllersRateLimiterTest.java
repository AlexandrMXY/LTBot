package backend.academy.scrapper.controllers;

import backend.academy.api.model.NotificationPolicy;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.LinkTagRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.api.model.requests.TagsRequest;
import backend.academy.scrapper.service.LinksService;
import backend.academy.scrapper.service.TagsService;
import backend.academy.scrapper.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.spring6.ratelimiter.configure.RateLimiterConfiguration;
import io.github.resilience4j.springboot3.ratelimiter.autoconfigure.RateLimiterAutoConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;



import java.util.stream.Stream;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest
@EnableRetry
@Import({RateLimiterAutoConfiguration.class})
public class ControllersRateLimiterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TagsService tagsService;
    @MockitoBean
    private LinksService linksService;

    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    private static String json(Object object) {
        return mapper.writeValueAsString(object);
    }

    public static Stream<Arguments> requests() {
        return Stream.of(
            Arguments.of(post("/tg-chat/444")),
            Arguments.of(delete("/tg-chat/444")),
            Arguments.of(get("/links")
                .header("Tg-Chat-Id", 44)),
            Arguments.of(post("/links")
                .header("Tg-Chat-Id", 44)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new AddLinkRequest(null, null, null)))),
            Arguments.of(delete("/links")
                .header("Tg-Chat-Id", 44)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new RemoveLinkRequest(null)))),
            Arguments.of(delete("/links/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new LinkTagRequest(0, null, null)))),
            Arguments.of(post("/links/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new LinkTagRequest(0, null, null)))),
            Arguments.of(get("/notifications/11/policy")),
            Arguments.of(post("/notifications/11/policy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new NotificationPolicy(null, null)))),
            Arguments.of(post("/tags/deactivate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new TagsRequest(0, null)))),
            Arguments.of(post("/tags/reactivate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(new TagsRequest(0, null)))),
            Arguments.of(get("/tags/111")),
            Arguments.of(get("/tags/linksWithTag")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new TagsRequest(0, null)))));
    }

    @ParameterizedTest
    @MethodSource("requests")
    public void endpoint_requestLimitExceeded_returns429(RequestBuilder request) throws Exception {
        int RATE_LIMIT = 10;
        for (int i = 0; i < RATE_LIMIT; i++) {
            try {
                mockMvc.perform(request).andExpect((res) -> {});
            } catch (Exception e) {}
        }
        mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().is(429));
    }
}
