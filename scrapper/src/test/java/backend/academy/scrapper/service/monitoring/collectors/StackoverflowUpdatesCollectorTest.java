package backend.academy.scrapper.service.monitoring.collectors;

import backend.academy.scrapper.RestClientsConfiguration;
import backend.academy.scrapper.SpringTestConfig;
import backend.academy.scrapper.TestUtils;
import backend.academy.scrapper.dto.updates.StackoverflowAnswerUpdate;
import backend.academy.scrapper.dto.updates.StackoverflowCommentUpdate;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import java.util.List;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {GithubUpdatesCollector.class})
@Import({SpringTestConfig.class, RestClientsConfiguration.class})
class StackoverflowUpdatesCollectorTest {
    @Autowired
    private StackoverflowUpdatesCollector collector;

    private static final int PORT = 8083;
    private WireMockServer wireMockServer;

    @BeforeEach
    public void init() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options()
            .port(PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", PORT);
    }

    @AfterEach
    public void shutdown() {
        if (wireMockServer != null)
            wireMockServer.stop();
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
                .withBody(TestUtils.getResponseJson("so/answersResponse1.json"))
            ));
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
            .withQueryParam("site", equalTo("stackoverflow"))
            .withQueryParam("page", equalTo("2"))
            .withPathParam("ids", equalTo("321"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/answersResponse2.json"))
            ));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/commentsEmptyResponse.json"))
            ));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
            0,
            new User(10, List.of()),
            "http://127.0.0.1",
            "stackoverflowMonitor",
            List.of(),
            List.of(),
            "321",
            100L
        )));

        assertThatIterable(updates.getUpdates())
            .size().isEqualTo(6).returnToIterable()
            .satisfiesExactlyInAnyOrder(
                    u -> assertEquals("Lorem ipsum 1", ((StackoverflowAnswerUpdate)u).content()),
                    u -> assertEquals("Lorem ipsum 2", ((StackoverflowAnswerUpdate)u).content()),
                    u -> assertEquals("Lorem ipsum 3", ((StackoverflowAnswerUpdate)u).content()),
                    u -> assertEquals("Lorem ipsum 4", ((StackoverflowAnswerUpdate)u).content()),
                    u -> assertEquals("Lorem ipsum 5", ((StackoverflowAnswerUpdate)u).content()),
                    u -> assertEquals("Lorem ipsum 7", ((StackoverflowAnswerUpdate)u).content()))
            .allSatisfy(upd -> {
                StackoverflowAnswerUpdate u = (StackoverflowAnswerUpdate)upd;
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
                .withBody(TestUtils.getResponseJson("so/answersResponse2.json"))
            ));
        stubFor(get(urlPathTemplate("/questions/{ids}/answers"))
            .withQueryParam("site", equalTo("stackoverflow"))
            .withQueryParam("page", equalTo("1"))
            .withPathParam("ids", equalTo("2"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/answersResponse3.json"))
            ));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/commentsEmptyResponse.json"))
            ));


        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
            0,
            new User(10, List.of()),
            "http://127.0.0.1",
            "stackoverflowMonitor",
            List.of(),
            List.of(),
            "1",
            100L
        ),new TrackedLink(
            0,
            new User(20, List.of()),
            "http://127.0.0.1",
            "stackoverflowMonitor",
            List.of(),
            List.of(),
            "2",
            100L
        )));

        assertThatIterable(updates.getUpdates())
            .satisfiesExactlyInAnyOrder(u -> {
                assertEquals("Lorem ipsum 4", ((StackoverflowAnswerUpdate)u).content());
                assertEquals(10L, ((StackoverflowAnswerUpdate)u).user());
            }, u -> {
                assertEquals("Lorem ipsum 5", ((StackoverflowAnswerUpdate)u).content());
                assertEquals(10L, ((StackoverflowAnswerUpdate)u).user());
            }, u -> {
                assertEquals("Lorem ipsum 7", ((StackoverflowAnswerUpdate)u).content());
                assertEquals(10L, ((StackoverflowAnswerUpdate)u).user());
            }, u -> {
                assertEquals("Lorem ipsum 8", ((StackoverflowAnswerUpdate)u).content());
                assertEquals(20L, ((StackoverflowAnswerUpdate)u).user());
            }, u -> {
                assertEquals("Lorem ipsum 9", ((StackoverflowAnswerUpdate)u).content());
                assertEquals(20L, ((StackoverflowAnswerUpdate)u).user());
            }, u -> {
                assertEquals("Lorem ipsum 10", ((StackoverflowAnswerUpdate)u).content());
                assertEquals(20L, ((StackoverflowAnswerUpdate)u).user());
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
                .withBody(TestUtils.getResponseJson("so/answersResponse4.json"))
            ));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
            .withQueryParam("site", equalTo("stackoverflow"))
            .withQueryParam("page", equalTo("1"))
            .withPathParam("ids", equalTo("100"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/commentsResponse.json"))
            ));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
            0,
            new User(10, List.of()),
            "http://127.0.0.1",
            "stackoverflowMonitor",
            List.of(),
            List.of(),
            "1",
            100L
        )));

        assertThatIterable(updates.getUpdates())
            .satisfiesExactlyInAnyOrder((u) -> {
                assertEquals(10, ((StackoverflowCommentUpdate)u).user());
                assertEquals("Comment 1", ((StackoverflowCommentUpdate)u).content());
            }, (u) -> {
                assertEquals(10, ((StackoverflowCommentUpdate)u).user());
                assertEquals("Comment 2", ((StackoverflowCommentUpdate)u).content());
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
                .withBody(TestUtils.getResponseJson("so/answersResponse4.json"))
            ));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/commentsEmptyResponse.json"))
            ));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
            0,
            new User(10, List.of()),
            "http://127.0.0.1",
            "stackoverflowMonitor",
            List.of(),
            List.of(),
            "1",
            10L
        )));

        assertThatIterable(updates.getUpdates())
            .satisfiesExactlyInAnyOrder((upd) -> {
                StackoverflowAnswerUpdate u = (StackoverflowAnswerUpdate) upd;
                assertEquals(10, u.user());
                assertEquals("Ans 1", u.content());
                assertEquals(50, u.date());
                assertEquals("User1", u.creator());
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
                .withBody(TestUtils.getResponseJson("so/answersResponse4.json"))
            ));
        stubFor(get(urlPathTemplate("/answers/{ids}/comments"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(TestUtils.getResponseJson("so/commentsResponse.json"))
            ));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
            0,
            new User(10, List.of()),
            "http://127.0.0.1",
            "stackoverflowMonitor",
            List.of(),
            List.of(),
            "1",
            100L
        )));

        assertThatIterable(updates.getUpdates())
            .satisfiesExactlyInAnyOrder((upd) -> {
                StackoverflowCommentUpdate u = (StackoverflowCommentUpdate) upd;
                assertEquals(10, u.user());
                assertEquals("Comment 1", u.content());
                assertEquals(1000, u.date());
                assertEquals("User1", u.creator());
            }, (upd) -> {
                StackoverflowCommentUpdate u = (StackoverflowCommentUpdate) upd;
                assertEquals(10, u.user());
                assertEquals("Comment 2", u.content());
                assertEquals(1000, u.date());
                assertEquals("User2", u.creator());
            });
    }
}
