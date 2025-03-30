package backend.academy.scrapper.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import backend.academy.api.model.LinkUpdate;
import backend.academy.scrapper.dto.updates.UpdateImpl;
import backend.academy.scrapper.dto.updates.Updates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
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
class HttpBotNotifierServiceTest {
    private static final String BOT_URL = "http://localhost:8080";

    @Spy
    private RestClient botClient = RestClient.builder().baseUrl(BOT_URL).build();

    @InjectMocks
    private HttpBotNotifierService service;

    private WireMockServer wireMock;

    @BeforeEach
    public void init() {
        wireMock = new WireMockServer(8080);
        wireMock.start();
        WireMock.configureFor("localhost", 8080);
    }

    @AfterEach
    void teardown() {
        if (wireMock != null) {
            wireMock.stop();
        }
    }

    @Test
    public void sendUpdates_noUpdatesPassed_doNothing() {
        service.sendUpdates(new Updates());
        verify(0, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void sendUpdates_nullPassed_doNothing() {
        service.sendUpdates(null);
        verify(0, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void sendUpdates_updatesPassed_requestSent() throws JsonProcessingException {
        Updates u = new Updates();
        u.addUpdate(new UpdateImpl(1L, 11, "A", "A", "B"));
        u.addUpdate(new UpdateImpl(2L, 11, "AA", "AA", "BB"));

        stubFor(post("/updates").willReturn(aResponse()));

        service.sendUpdates(u);

        List<ServeEvent> allServeEvents = getAllServeEvents();
        assertThat(allServeEvents.size()).isEqualTo(2);
        var body1 = allServeEvents.get(0).getRequest().getBodyAsString();
        var body2 = allServeEvents.get(1).getRequest().getBodyAsString();

        LinkUpdate l1 = new ObjectMapper().readValue(body1, LinkUpdate.class);
        LinkUpdate l2 = new ObjectMapper().readValue(body2, LinkUpdate.class);

        LinkUpdate expected1 = new LinkUpdate(1L, 11, "A", "A", "B");
        LinkUpdate expected2 = new LinkUpdate(2L, 11, "AA", "AA", "BB");

        assertThatIterable(List.of(l1, l2)).containsExactlyInAnyOrderElementsOf(List.of(expected1, expected2));
    }
}
