package backend.academy.scrapper.service;

import backend.academy.api.model.LinkUpdate;
import backend.academy.scrapper.service.monitoring.Updates;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import java.util.List;
import java.util.Objects;
import static org.assertj.core.api.Assertions.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@ExtendWith(MockitoExtension.class)
class BotServiceTest {
    private static final String BOT_URL = "http://localhost:8080";

    @Spy
    private RestClient botClient = RestClient.builder()
        .baseUrl(BOT_URL)
        .build();

    @InjectMocks
    private BotService service;

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
        u.addUpdate(new Updates.Update(List.of(1L), "A", "B"));
        u.addUpdate(new Updates.Update(List.of(2L), "AA", "BB"));

        stubFor(post("/updates").willReturn(aResponse()));

        service.sendUpdates(u);

        List<ServeEvent> allServeEvents = getAllServeEvents();
        assertThat(allServeEvents.size()).isEqualTo(2);
        var body1 = allServeEvents.get(0).getRequest().getBodyAsString();
        var body2 = allServeEvents.get(1).getRequest().getBodyAsString();

        LinkUpdate l1 = new ObjectMapper().readValue(body1, LinkUpdate.class);
        LinkUpdate l2 = new ObjectMapper().readValue(body2, LinkUpdate.class);

        LinkUpdate expected1 = new LinkUpdate(0, "A", "B", List.of(1L));
        LinkUpdate expected2 = new LinkUpdate(0, "AA", "BB", List.of(2L));

        assertThat((compareLinkUpdates(l1, expected1) && compareLinkUpdates(l2, expected2))
            || (compareLinkUpdates(l2, expected1) && compareLinkUpdates(l1, expected2))).isTrue();
    }

    public boolean compareLinkUpdates(LinkUpdate l1, LinkUpdate l2) {
        return
            Objects.equals(l1.url(), l2.url()) && compareLists(l1.tgChatIds(), l2.tgChatIds()) && Objects.equals(l1.description(), l2.description());
    }

    public <T> boolean compareLists(List<T> l1, List<T> l2) {
        try {
            assertThatList(l1).containsExactlyInAnyOrderElementsOf(l2);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
