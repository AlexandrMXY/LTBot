package backend.academy.scrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.RemoveLinkRequest;
import backend.academy.scrapper.controllers.LinksController;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@Import(SpringTestConfig.class)
@AutoConfigureMockRestServiceServer
public class LinksAddRemoveTest {
    @MockitoBean
    public UserRepository userRepository;

    @MockitoBean
    public LinkRepository linkRepository;

    @Autowired
    private LinksController linksController;

    private final User existingUser = new User(
            0,
            new ArrayList<>(List.of(new TrackedLink(
                    777,
                    null,
                    "https://github.com/qwerty/qwerty",
                    "githubMonitor",
                    List.of(),
                    List.of(),
                    "qwerty/qwerty"))));

    @BeforeEach
    public void init() {
        when(userRepository.existsById(eq(0L))).thenReturn(true);
        when(userRepository.findById(eq(0L))).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    public void addLinks_addGithubLink_success() {
        when(linkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        var response =
                linksController.addLinks(0, new AddLinkRequest("https://github.com/aaa/bbb", List.of(), List.of()));

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

        var response = linksController.addLinks(
                0,
                new AddLinkRequest(
                        "stackoverflow.com/questions/14938748/howdqhhfjienk1njqifhbhjhjio", List.of(), List.of()));

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
        assertThrows(
                UnsupportedLinkException.class,
                () -> linksController.addLinks(0, new AddLinkRequest("youtube.com", List.of(), List.of())));

        verify(linkRepository, never()).save(any());
    }

    @Test
    public void addLink_duplicatePassed_alreadyExistsException() {
        doReturn(true)
                .when(linkRepository)
                .existsByUserAndMonitoringServiceAndServiceId(
                        eq(existingUser), eq("githubMonitor"), eq("qwerty/qwerty"));
        assertThrows(
                AlreadyExistsException.class,
                () -> linksController.addLinks(
                        0, new AddLinkRequest("https://github.com/qwerty/qwerty", List.of(), List.of())));
    }

    @Test
    public void deleteLink_validLink_shouldDelete() {
        linksController.deleteLinks(0, new RemoveLinkRequest("https://github.com/qwerty/qwerty"));

        verify(linkRepository, only()).deleteById(eq(777L));
    }
}
