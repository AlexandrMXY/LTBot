package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.service.monitoring.LinkDistributionService;
import backend.academy.scrapper.service.monitoring.LinkMonitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinksServiceTest {
    @Spy
    private ScrapperConfig scrapperConfig = new ScrapperConfig(
        "",
        null,
        ".*",
        "",
        "",
        "",
        null,
        null,
        10,
        10,
        new ScrapperConfig.KafkaTopics("updates", "dead-letters"));

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LinkDistributionService linkDistributionService;

    @InjectMocks
    private LinksService linksService;

    @BeforeEach
    public void init() {
        try {
            var m = linksService.getClass().getDeclaredMethod("init");
            m.setAccessible(true);
            m.invoke(linksService);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Test
    public void addLink_invalidTags_shouldThrow() {
        LinkDto link = new LinkDto("", List.of("q"), List.of(""), 0);

        assertThrows(InvalidRequestException.class, () -> linksService.addLink(0, link));
    }

    @Test
    public void addLinks_unsupportedLink_shouldThrow() {
        when(linkDistributionService.findMonitor(any())).thenReturn(null);

        LinkDto link = new LinkDto("", List.of("f"), List.of(""), 0);

        assertThrows(UnsupportedLinkException.class, () -> linksService.addLink(0, link));
    }

    @Test
    public void addLinks_userExists_shouldModifyUser() {
        when(linkRepository.existsByUserAndMonitoringServiceAndServiceId(any(), any(), any()))
                .thenReturn(false);
        long id = 0;
        User user = new User(id, listOf());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(linkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(linkDistributionService.findMonitor(any())).thenReturn("monitor");
        LinkMonitor monitor = mock(LinkMonitor.class);
        when(monitor.getLinkId(any())).thenReturn("sid");
        when(linkDistributionService.getMonitor(eq("monitor"))).thenReturn(monitor);

        LinkDto link = new LinkDto("", List.of("f"), List.of(""), 0);

        linksService.addLink(id, link);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<TrackedLink> linkCaptor = ArgumentCaptor.forClass(TrackedLink.class);
        verify(userRepository).save(userCaptor.capture());
        verify(linkRepository).save(linkCaptor.capture());

        assertEquals(new User(id, List.of(linkCaptor.getValue())), userCaptor.getValue());
    }

    @Test
    public void addLinks_whenNoThrow_shouldSaveCorrectLink() {
        when(linkRepository.existsByUserAndMonitoringServiceAndServiceId(any(), any(), any()))
                .thenReturn(false);
        long id = 0;
        User user = new User(id, listOf());
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(linkRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        when(linkDistributionService.findMonitor(any())).thenReturn("monitor");
        LinkMonitor monitor = mock(LinkMonitor.class);
        when(monitor.getLinkId(any())).thenReturn("sid");
        when(linkDistributionService.getMonitor(eq("monitor"))).thenReturn(monitor);

        LinkDto link = new LinkDto("", List.of("f"), List.of(""), 0);

        linksService.addLink(id, link);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<TrackedLink> linkCaptor = ArgumentCaptor.forClass(TrackedLink.class);
        verify(userRepository).save(userCaptor.capture());
        verify(linkRepository).save(linkCaptor.capture());

        assertEquals(userCaptor.getValue().id(), linkCaptor.getValue().user().id());
        assertEquals(
                new TrackedLink(
                        linkCaptor.getValue().id(),
                        userCaptor.getValue(),
                        link.link(),
                        "monitor",
                        link.tags(),
                        link.filters(),
                        "sid",
                        linkCaptor.getValue().lastUpdate()),
                linkCaptor.getValue());
    }

    @Test
    public void deleteLink_userDoesntExists_throwNotFoundException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> linksService.deleteLink(0, ""));
    }

    @Test
    public void deleteLink_linkDoesntExists_throwNotFoundException() {
        when(userRepository.findById(eq(0L))).thenReturn(Optional.of(new User(0, List.of())));

        assertThrows(NotFoundException.class, () -> linksService.deleteLink(0, "qwerty"));
    }

    @Test
    public void deleteLink_success_returnRemovedLink() {
        TrackedLink link = new TrackedLink(1, null, "qwerty", null, null, null, null, 0);
        User user = new User(0, listOf(link));
        when(userRepository.findById(eq(0L))).thenReturn(Optional.of(user));

        assertEquals(new LinkDto(link), linksService.deleteLink(0, "qwerty"));
    }

    @Test
    public void deleteLink_success_shouldSaveChanges() {
        TrackedLink link = new TrackedLink(1, null, "qwerty", null, null, null, null, 0);
        User user = new User(0, listOf(link));
        when(userRepository.findById(eq(0L))).thenReturn(Optional.of(user));

        linksService.deleteLink(0, "qwerty");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Long> linkIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).save(userCaptor.capture());
        verify(linkRepository).deleteById(linkIdCaptor.capture());

        assertEquals(new User(0, List.of()), userCaptor.getValue());
        assertEquals(link.id(), linkIdCaptor.getValue());
    }

    @Test
    public void getLinks_success_shouldReturnListOfLinks() {
        TrackedLink link1 = new TrackedLink(1, null, "qwerty1", null, null, null, null, 0);
        TrackedLink link2 = new TrackedLink(2, null, "qwerty2", null, null, null, null, 0);
        TrackedLink link3 = new TrackedLink(3, null, "qwerty3", null, null, null, null, 0);

        when(userRepository.findById(eq(0L))).thenReturn(Optional.of(new User(0, List.of(link1, link2, link3))));

        var res = linksService.getLinks(0);
        var expected = List.of(new LinkDto(link1), new LinkDto(link2), new LinkDto(link3));

        assertIterableEquals(expected, res);
    }

    @SafeVarargs
    private static <T> List<T> listOf(T... args) {
        return new ArrayList<>(Arrays.asList(args));
    }
}
