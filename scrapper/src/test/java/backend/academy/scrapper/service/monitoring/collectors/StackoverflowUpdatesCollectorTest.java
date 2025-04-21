package backend.academy.scrapper.service.monitoring.collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.model.LinkUpdate;
import backend.academy.scrapper.SpringTestConfig;
import backend.academy.scrapper.TestUtils;
import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.entities.filters.Filters;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {GithubUpdatesCollector.class})
@Import({SpringTestConfig.class})
class StackoverflowUpdatesCollectorTest {
    @Autowired
    private StackoverflowUpdatesCollector collector;

    private static final int PORT = 8083;
    private WireMockServer wireMockServer;

    @BeforeEach
    public void init() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", PORT);
    }

    @AfterEach
    public void shutdown() {
        if (wireMockServer != null) wireMockServer.stop();
    }

    @Test
    public void getUpdates_multiplePagesResponse_shouldReturnAllUpdates() {
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("321"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse1.json"))));
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("2"))
                .withPathParam("ids", equalTo("321"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse2.json"))));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/commentsEmptyResponse.json"))));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
                0,
                new User(10, List.of()),
                "http://127.0.0.1",
                "stackoverflowMonitor",
                List.of(),
                new Filters(),
                "321",
                100L)));

        assertThatIterable(updates.getUpdates())
                .size()
                .isEqualTo(6)
                .returnToIterable()
                .satisfiesExactlyInAnyOrder(
                        u -> assertEquals("Lorem ipsum 1", u.createRequest().content()),
                        u -> assertEquals("Lorem ipsum 2", u.createRequest().content()),
                        u -> assertEquals("Lorem ipsum 3", u.createRequest().content()),
                        u -> assertEquals("Lorem ipsum 4", u.createRequest().content()),
                        u -> assertEquals("Lorem ipsum 5", u.createRequest().content()),
                        u -> assertEquals("Lorem ipsum 7", u.createRequest().content()))
                .allSatisfy(upd -> {
                    Update u = (Update) upd;
                    assertEquals(10, u.user());
                    assertThat(u.date()).isGreaterThan(100);
                });
    }

    @Test
    public void getUpdates_multipleUsers_shouldReturnAllUpdates() {
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse2.json"))));
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse3.json"))));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/commentsEmptyResponse.json"))));

        Updates updates = collector.getUpdates(Stream.of(
                new TrackedLink(
                        0,
                        new User(10, List.of()),
                        "http://127.0.0.1",
                        "stackoverflowMonitor",
                        List.of(),
                        new Filters(),
                        "1",
                        100L),
                new TrackedLink(
                        0,
                        new User(20, List.of()),
                        "http://127.0.0.1",
                        "stackoverflowMonitor",
                        List.of(),
                        new Filters(),
                        "2",
                        100L)));

        assertThatIterable(updates.getUpdates())
                .satisfiesExactlyInAnyOrder(
                        u -> {
                            assertEquals("Lorem ipsum 4", u.createRequest().content());
                            assertEquals(10L, u.createRequest().chatId());
                        },
                        u -> {
                            assertEquals("Lorem ipsum 5", u.createRequest().content());
                            assertEquals(10L, u.createRequest().chatId());
                        },
                        u -> {
                            assertEquals("Lorem ipsum 7", u.createRequest().content());
                            assertEquals(10L, u.createRequest().chatId());
                        },
                        u -> {
                            assertEquals("Lorem ipsum 8", u.createRequest().content());
                            assertEquals(20L, u.createRequest().chatId());
                        },
                        u -> {
                            assertEquals("Lorem ipsum 9", u.createRequest().content());
                            assertEquals(20L, u.createRequest().chatId());
                        },
                        u -> {
                            assertEquals("Lorem ipsum 10", u.createRequest().content());
                            assertEquals(20L, u.createRequest().chatId());
                        });
    }

    @Test
    public void getUpdates_hasComments_shouldReturnCommentsUpdates() {
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse4.json"))));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("100"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/commentsResponse.json"))));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
                0,
                new User(10, List.of()),
                "http://127.0.0.1",
                "stackoverflowMonitor",
                List.of(),
                new Filters(),
                "1",
                100L)));

        assertThatIterable(updates.getUpdates())
                .satisfiesExactlyInAnyOrder(
                        (u) -> {
                            assertEquals(10, u.createRequest().chatId());
                            assertEquals("Comment 1", u.createRequest().content());
                        },
                        (u) -> {
                            assertEquals(10, u.createRequest().chatId());
                            assertEquals("Comment 2", u.createRequest().content());
                        });
    }

    @Test
    public void getUpdates_answersUpdate_shouldContainAllRequiredInfo() {
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse4.json"))));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/commentsEmptyResponse.json"))));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
                0,
                new User(10, List.of()),
                "http://127.0.0.1",
                "stackoverflowMonitor",
                List.of(),
                new Filters(),
                "1",
                10L)));

        assertThatIterable(updates.getUpdates()).satisfiesExactlyInAnyOrder((upd) -> {
            LinkUpdate u = upd.createRequest();
            assertEquals(10, u.chatId());
            assertEquals("Ans 1", u.content());
            assertEquals(50, u.time());
            assertEquals("User1", u.author());
        });
    }

    @Test
    public void getUpdates_commentsUpdate_shouldContainAllRequiredInfo() {
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("page", equalTo("1"))
                .withPathParam("ids", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/answersResponse4.json"))));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("so/commentsResponse.json"))));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
                0,
                new User(10, List.of()),
                "http://127.0.0.1",
                "stackoverflowMonitor",
                List.of(),
                new Filters(),
                "1",
                100L)));

        assertThatIterable(updates.getUpdates())
                .satisfiesExactlyInAnyOrder(
                        (upd) -> {
                            LinkUpdate u = upd.createRequest();
                            assertEquals(10, u.chatId());
                            assertEquals("Comment 1", u.content());
                            assertEquals(1000L, u.time());
                            assertEquals("User1", u.author());
                        },
                        (upd) -> {
                            LinkUpdate u = upd.createRequest();
                            assertEquals(10, u.chatId());
                            assertEquals("Comment 2", u.content());
                            assertEquals(1000L, u.time());
                            assertEquals("User2", u.author());
                        });
    }
}
