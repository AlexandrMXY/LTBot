package backend.academy.scrapper.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.SpringTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.ErrorResponseException;

@SpringBootTest(classes = {GithubService.class})
@Import(SpringTestConfig.class)
@WireMockTest(httpPort = 8082)
class GithubServiceTest {
    @Autowired
    private GithubService githubService;

    @Test
    public void getUpdates_errorResponse_exception() {
        stubFor(get(urlMatching("/repos/.*")).willReturn(aResponse().withStatus(400)));

        assertThrows(ErrorResponseException.class, () -> githubService.getUpdates(Stream.of("qwerty/qwerty"), 0L));
    }

    @Test
    public void getUpdates_invalidBody_exception() {
        stubFor(get(urlMatching("/repos/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"nothing here\":\"nothing here\"}")));

        assertThrows(RuntimeException.class, () -> githubService.getUpdates(Stream.of("qwerty/qwerty"), 0L));
    }
}
