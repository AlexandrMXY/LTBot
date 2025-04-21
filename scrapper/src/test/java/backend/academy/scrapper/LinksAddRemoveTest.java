package backend.academy.scrapper;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.scrapper.controllers.LinksController;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

// @SpringBootTest(classes = {
//    LinksController.class,
//    LinksManagementService.class,
//    LinkDistributionService.class,
//    GithubMonitor.class,
//    GithubUpdatesCollector.class,
//    GithubRestClient.class,
//    StackoverflowMonitor.class,
//    StackoverflowUpdatesCollector.class,
//    StackoverflowRestClient.class
// })
@SpringBootTest
@EnableWebMvc
public class LinksAddRemoveTest extends AbstractAppTest {
    @MockitoSpyBean
    public UserRepository userRepository;

    @MockitoSpyBean
    public LinkRepository linkRepository;

    @Autowired
    private LinksController linksController;

    @Test
    @Transactional
    public void addLinks_addGithubLink_success() {

        var response =
                linksController.addLinks(10, new AddLinkRequest("https://github.com/aaa/bbb", List.of(), List.of()));

        User u = userRepository.findById(10L).orElseThrow();

        TrackedLink link = u.links().stream()
                .filter(l ->
                        l.serviceId().equals("aaa/bbb") && l.monitoringService().equals("githubMonitor"))
                .findAny()
                .orElseThrow();

        assertEquals(10L, u.id());
        assertTrue(link.filters().isEmpty());
        assertTrue(link.tags().isEmpty());

        assertEquals("https://github.com/aaa/bbb", response.url());
    }

    @Test
    @Transactional
    public void addLinks_addStackoverflowLink_success() {
        var response = linksController.addLinks(
                10,
                new AddLinkRequest(
                        "stackoverflow.com/questions/14938748/howdqhhfjienk1njqifhbhjhjio", List.of(), List.of()));

        User u = userRepository.findById(10L).orElseThrow();

        TrackedLink link = u.links().stream()
                .filter(l -> l.serviceId().equals("14938748")
                        && l.monitoringService().equals("stackoverflowMonitor"))
                .findAny()
                .orElseThrow();

        assertEquals(10L, u.id());
        assertTrue(link.filters().isEmpty());
        assertTrue(link.tags().isEmpty());

        assertEquals("stackoverflow.com/questions/14938748/howdqhhfjienk1njqifhbhjhjio", response.url());
    }

    @Test
    public void addLinks_invalidLink_exception() {
        assertThrows(
                UnsupportedLinkException.class,
                () -> linksController.addLinks(0, new AddLinkRequest("youtube.com", List.of(), List.of())));
    }

    @Test
    public void addLink_duplicatePassed_alreadyExistsException() {
        linksController.addLinks(0, new AddLinkRequest("https://github.com/qwerty/qwerty", List.of(), List.of()));
        assertThrows(
                AlreadyExistsException.class,
                () -> linksController.addLinks(
                        0, new AddLinkRequest("https://github.com/qwerty/qwerty", List.of(), List.of())));
    }

    @Test
    public void deleteLink_linkNotFound_shouldThrow() {
        assertThrows(
                NotFoundException.class,
                () -> linksController.deleteLinks(0, new RemoveLinkRequest("https://github.com/Q111/Q111")));
    }

    @Test
    public void deleteLink_validLink_shouldDelete() {
        linksController.addLinks(0, new AddLinkRequest("https://github.com/Qw/Qw", List.of(), List.of()));
        linksController.addLinks(0, new AddLinkRequest("https://github.com/Qw/Qwa", List.of(), List.of()));

        linksController.deleteLinks(0, new RemoveLinkRequest("https://github.com/Qw/Qw"));

        assertThatIterable(linksController.getLinks(0).links())
                .allSatisfy((lnk) -> assertThat(lnk.url()).isNotEqualTo("https://github.com/Qw/Qw"));
    }
}
