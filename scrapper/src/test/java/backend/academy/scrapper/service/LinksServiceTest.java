package backend.academy.scrapper.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.AbstractAppTest;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.entities.filters.Filters;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@DirtiesContext
class LinksServiceTest extends AbstractAppTest {
    private static final String VALID_LINK =
            "https://stackoverflow.com/questions/64383424/set-anonymous-dynamic-functions-to-menu";

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LinksService linksService;

    @Test
    public void addLink_invalidTags_shouldThrow() {
        LinkDto link = new LinkDto(VALID_LINK, List.of("^q3 `\\▬"), List.of(""), 0);

        assertThrows(InvalidRequestException.class, () -> linksService.addLink(0, link));
    }

    @Test
    public void addLinks_unsupportedLink_shouldThrow() {
        LinkDto link = new LinkDto("qhwnhjyqthnwmhyjuyqtqrhtwmhejrlukyeutytre", List.of("f"), List.of(""), 0);
        assertThrows(UnsupportedLinkException.class, () -> linksService.addLink(0, link));
    }

    @Test
    public void addLinks_userExists_shouldModifyUser() {
        userRepository.save(new User(100));
        LinkDto link = new LinkDto(VALID_LINK, List.of("f"), List.of(""), 0);
        linksService.addLink(100, link);
        assertEquals(1, userRepository.findById(100).orElseThrow().links().size());
    }

    @Test
    public void addLinks_whenSuccess_shouldSaveCorrectLink() {
        userRepository.save(new User(101));
        LinkDto link = new LinkDto(VALID_LINK, List.of("f", "g"), List.of(), 0);
        linksService.addLink(101, link);

        TrackedLink actual = userRepository.findById(101).orElseThrow().links().getFirst();

        assertEquals(link.link(), actual.url());
        Assertions.assertThat(actual.tags()).containsExactlyInAnyOrderElementsOf(link.tags());
    }

    @Test
    public void deleteLink_userDoesntExists_throwNotFoundException() {
        assertThrows(NotFoundException.class, () -> linksService.deleteLink(74787858, ""));
    }

    @Test
    public void deleteLink_linkDoesntExists_throwNotFoundException() {
        userRepository.save(new User(102));
        assertThrows(NotFoundException.class, () -> linksService.deleteLink(102, "qwerty"));
    }

    @Test
    public void deleteLink_success_returnRemovedLink() {
        userRepository.save(new User(103));
        LinkDto link = new LinkDto(VALID_LINK, List.of("f"), List.of(), 0);
        linksService.addLink(103, link);
        var actual = linksService.deleteLink(103, VALID_LINK);
        assertEquals(link.link(), actual.link());
        assertEquals(link.tags(), actual.tags());
        assertEquals(link.filters(), actual.filters());
    }

    @Test
    public void deleteLink_success_shouldSaveChanges() {
        userRepository.save(new User(103));
        linksService.addLink(103, new LinkDto(VALID_LINK, List.of("f"), List.of(), 0));
        linksService.deleteLink(103, VALID_LINK);
        assertFalse(linkRepository.existsByUserAndMonitoringServiceAndServiceId(
                new User(103L), "stackoverflowMonitor", "64383424"));
    }

    @Test
    public void getLinks_success_shouldReturnListOfLinks() {
        User user = new User(104);
        TrackedLink link1 = new TrackedLink(1, user, "qwerty1", null, null, new Filters(), null, 0);
        TrackedLink link2 = new TrackedLink(2, user, "qwerty2", null, null, new Filters(), null, 0);
        TrackedLink link3 = new TrackedLink(3, user, "qwerty3", null, null, new Filters(), null, 0);
        user.links(new ArrayList<>(List.of(link1, link2, link3)));
        userRepository.save(user);

        var res = linksService.getLinks(104);
        var expected = List.of(new LinkDto(link1), new LinkDto(link2), new LinkDto(link3));

        assertIterableEquals(expected, res);
    }
}
