package backend.academy.scrapper.service;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.SpringTestConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.ErrorResponseException;

@SpringBootTest(classes = {StackoverflowService.class})
@Import(SpringTestConfig.class)
@WireMockTest(httpPort = 8083)
class StackoverflowServiceTest {
    @Autowired
    private StackoverflowService stackoverflowService;

    @Test
    public void getUpdates_errorResponse_exception() {
        stubFor(get(urlMatching("/questions/.*/answers/.*"))
                .willReturn(aResponse().withStatus(400)));

        assertThrows(ErrorResponseException.class, () -> stackoverflowService.getUpdates(List.of(666L, 777L), 0L));
    }

    @Test
    public void getUpdates_invalidBody_exception() {
        stubFor(get(urlMatching("/repos/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"nothing here\":\"nothing here\"}")));

        assertThrows(RuntimeException.class, () -> stackoverflowService.getUpdates(List.of(666L, 777L), 0L));
    }
}
