package backend.academy.bot.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.bot.BotConfig;
import backend.academy.bot.BotSpringConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class ScrapperServiceTest {
    public static final String SCRAPPER_URL = "http://localhost:8080";
    private static final BotConfig TEST_CONFIG = new BotConfig("", SCRAPPER_URL, 100000,
        new BotConfig.KafkaTopics("updates", "dead-letters"));

    @Spy
    private RestClient restClient = new BotSpringConfig().scrapperRestClient(TEST_CONFIG);

    @InjectMocks
    private ScrapperService service = new ScrapperService();

    private WireMockServer wireMock;

    @BeforeEach
    public void init() {
        wireMock = new WireMockServer(8080);
        wireMock.start();
        WireMock.configureFor("localhost", 8080);
    }

    @Test
    public void addLink_whenReceivedErrorResponse_throwApiErrorResponseException() {
        long chat = 888;
        var request = new AddLinkRequest("", List.of(), List.of());

        stubFor(
                post(urlPathEqualTo("/links"))
                        .withHeader("Tg-Chat-Id", equalTo(String.valueOf(chat)))
                        .willReturn(
                                aResponse()
                                        .withStatus(400)
                                        .withBody(
                                                "{\"description\":\"Bad Request\",\"code\":\"400\",\"exceptionName\":\"backend.academy.scrapper.exceptions.AlreadyExistsException\",\"exceptionMessage\":\"Link already exists\",\"stacktrace\":[\"\"]}")));

        assertThrows(ApiErrorResponseException.class, () -> service.addLink(chat, request));
    }

    @AfterEach
    void teardown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }
}
