package backend.academy.scrapper;

import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.scrapper.controllers.LinksController;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.MonitoringServiceDataRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.service.LinksManagementService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(SpringTestConfig.class)
@AutoConfigureMockRestServiceServer
public class LinksAddRemoveTest  {
    @MockitoBean
    public UserRepository userRepository;
    @MockitoBean
    public LinkRepository linkRepository;
    @MockitoBean
    public MonitoringServiceDataRepository monitoringServiceDataRepository;

    @Autowired
    public MockClientsHolder clientsMocks;

    @Autowired
    private LinksController linksController;

    private User existingUser = new User(0, new ArrayList<>(List.of(
        new TrackedLink(777, null, "https://github.com/qwerty/qwerty",
            "githubMonitor", List.of(), List.of(), "qwerty/qwerty")
    )));

    @BeforeEach
    public void init() {
        when(userRepository.existsById(eq(0L))).thenReturn(true);
        when(userRepository.findById(eq(0L))).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }


    @Test
    public void addLinks_addGithubLink_success() {
        when(linkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = linksController.addLinks(0, new AddLinkRequest("https://github.com/aaa/bbb", List.of(), List.of()));

        ArgumentCaptor<TrackedLink> linkCaptor = ArgumentCaptor.forClass(TrackedLink.class);
        verify(linkRepository).save(linkCaptor.capture());

        TrackedLink saved = linkCaptor.getValue();

        assertEquals(0, saved.user().id());
        assertEquals("https://github.com/aaa/bbb", saved.url());
        assertEquals("githubMonitor", saved.monitoringService());
        assertEquals("aaa/bbb", saved.serviceId());
        assertTrue(saved.filters().isEmpty());
        assertTrue(saved.tags().isEmpty());

        assertEquals("https://github.com/aaa/bbb", response.url());
    }

    @Test
    public void addLinks_addStackoverflowLink_success() {
        when(linkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response = linksController.addLinks(0, new AddLinkRequest("stackoverflow.com/questions/14938748/howdqhhfjienk1njqifhbhjhjio", List.of(), List.of()));

        ArgumentCaptor<TrackedLink> linkCaptor = ArgumentCaptor.forClass(TrackedLink.class);
        verify(linkRepository).save(linkCaptor.capture());

        TrackedLink saved = linkCaptor.getValue();

        assertEquals(0, saved.user().id());
        assertEquals("stackoverflow.com/questions/14938748/howdqhhfjienk1njqifhbhjhjio", saved.url());
        assertEquals("stackoverflowMonitor", saved.monitoringService());
        assertEquals("14938748", saved.serviceId());
        assertTrue(saved.filters().isEmpty());
        assertTrue(saved.tags().isEmpty());

        assertEquals("stackoverflow.com/questions/14938748/howdqhhfjienk1njqifhbhjhjio", response.url());
    }

    @Test
    public void addLinks_invalidLink_exception() {
        assertThrows(UnsupportedLinkException.class,() ->
            linksController.addLinks(0, new AddLinkRequest("youtube.com", List.of(), List.of())));

        verify(linkRepository, never()).save(any());
    }

    @Test
    public void deleteLink_validLink_shouldDelete() {
        var response = linksController.deleteLinks(0, new RemoveLinkRequest("https://github.com/qwerty/qwerty"));

        verify(linkRepository, only()).deleteById(eq(777L));
    }
}
