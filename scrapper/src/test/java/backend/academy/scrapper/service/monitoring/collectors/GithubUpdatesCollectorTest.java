package backend.academy.scrapper.service.monitoring.collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.SpringTestConfig;
import backend.academy.scrapper.TestUtils;
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
class GithubUpdatesCollectorTest {
    @Autowired
    private GithubUpdatesCollector collector;

    private static final int PORT = 8082;
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

    private static final String DEFAULT_LINK_HEADER_RESPONSE = "</NEXT_PAGE>; rel=\"next\"";

    @Test
    public void getUpdates_multiplePagesResponse_shouldReturnAllUpdates() {
        stubFor(get(urlPathTemplate("/repos/{uId}/{rId}/issues"))
                .withQueryParam("since", equalTo("1970-01-01T00:01:40Z"))
                .withPathParam("uId", equalTo("qwe"))
                .withPathParam("rId", equalTo("rty"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("link", DEFAULT_LINK_HEADER_RESPONSE)
                        .withBody(TestUtils.getResponseJson("github/issuesResponse1.json"))));
        stubFor(get(urlEqualTo("/NEXT_PAGE"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("github/issuesResponse2.json"))));

        Updates updates = collector.getUpdates(Stream.of(new TrackedLink(
                0,
                new User(10, List.of()),
                "http://127.0.0.1",
                "githubMonitor",
                List.of(),
                new Filters(),
                "qwe/rty",
                100L)));

        assertEquals(6, updates.getUpdates().size());
        assertThatIterable(updates.getUpdates())
                .satisfiesExactlyInAnyOrder(
                        u -> {
                            assertEquals("https://localhost/qwerty2A", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AA", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAA", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAAA", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAAAA", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAAAAA", u.url());
                            assertEquals(10L, u.user());
                        });
    }

    @Test
    public void getUpdates_multipleUsers_shouldReturnAllUpdates() {
        stubFor(get(urlPathTemplate("/repos/{uId}/{rId}/issues"))
                .withQueryParam("since", equalTo("1970-01-01T00:01:40Z"))
                .withPathParam("uId", equalTo("qwe"))
                .withPathParam("rId", equalTo("rty"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("github/issuesResponse1.json"))));
        stubFor(get(urlPathTemplate("/repos/{uId}/{rId}/issues"))
                .withQueryParam("since", equalTo("1970-01-01T00:01:40Z"))
                .withPathParam("uId", equalTo("aaa"))
                .withPathParam("rId", equalTo("bbb"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(TestUtils.getResponseJson("github/issuesResponse2.json"))));

        Updates updates = collector.getUpdates(Stream.of(
                new TrackedLink(
                        0,
                        new User(10, List.of()),
                        "http://127.0.0.1",
                        "githubMonitor",
                        List.of(),
                        new Filters(),
                        "qwe/rty",
                        100L),
                new TrackedLink(
                        1,
                        new User(20, List.of()),
                        "http://127.0.0.1/1",
                        "githubMonitor",
                        List.of(),
                        new Filters(),
                        "aaa/bbb",
                        100L)));

        assertEquals(6, updates.getUpdates().size());
        assertThatIterable(updates.getUpdates())
                .satisfiesExactlyInAnyOrder(
                        u -> {
                            assertEquals("https://localhost/qwerty2A", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AA", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAA", u.url());
                            assertEquals(10L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAAA", u.url());
                            assertEquals(20L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAAAA", u.url());
                            assertEquals(20L, u.user());
                        },
                        u -> {
                            assertEquals("https://localhost/qwerty2AAAAAA", u.url());
                            assertEquals(20L, u.user());
                        });
    }
}
