package backend.academy.bot.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.bot.BotConfig;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScrapperServiceTest {
    public static final String SCRAPPER_URL = "http://localhost:8080";
    private static final BotConfig TEST_CONFIG = new BotConfig("", SCRAPPER_URL);

    private WireMockServer wireMock;

    @BeforeEach
    public void init() {
        wireMock = new WireMockServer(8080);
        wireMock.start();
        WireMock.configureFor("localhost", 8080);
    }

    @Test
    public void addLink_whenReceivedErrorResponse_throwApiErrorResponseException() {
        var service = new ScrapperService(TEST_CONFIG);
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
