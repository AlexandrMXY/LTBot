package backend.academy.scrapper.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.api.model.requests.TagsRequest;
import backend.academy.scrapper.AbstractAppTest;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.entities.filters.Filters;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TagsControllerTest extends AbstractAppTest {
    @Autowired
    TagsController tagsController;

    @Autowired
    UserRepository userRepository;

    @Test
    void deactivate_userNotFound_shouldThrow() {
        assertThrows(NotFoundException.class, () -> tagsController.deactivate(new TagsRequest(777, "tag")));
    }

    @Test
    void deactivate_tagInactive_shouldStayInactive() {
        userRepository.save(new User(2));
        assertTrue(tagsController
                .deactivate(new TagsRequest(2, "tag"))
                .getStatusCode()
                .is2xxSuccessful());
        assertTrue(userRepository.findById(2).orElseThrow().inactiveTags().contains("tag"));
    }

    @Test
    void deactivate_tagActive_shouldDeactivate() {
        userRepository.save(new User(3, List.of(), List.of("tag")));
        assertTrue(tagsController
                .deactivate(new TagsRequest(3, "tag"))
                .getStatusCode()
                .is2xxSuccessful());
        assertTrue(userRepository.findById(3).orElseThrow().inactiveTags().contains("tag"));
    }

    @Test
    void reactivate_tagInactive_shouldReactivate() {
        userRepository.save(new User(2));
        assertTrue(tagsController
                .reactivate(new TagsRequest(2, "tag"))
                .getStatusCode()
                .is2xxSuccessful());
        assertFalse(userRepository.findById(2).orElseThrow().inactiveTags().contains("tag"));
    }

    @Test
    void reactivate_tagActive_shouldStayActive() {
        userRepository.save(new User(3, List.of(), List.of("tag")));
        assertTrue(tagsController
                .reactivate(new TagsRequest(3, "tag"))
                .getStatusCode()
                .is2xxSuccessful());
        assertFalse(userRepository.findById(3).orElseThrow().inactiveTags().contains("tag"));
    }

    @Test
    void getLinksWithTag_userNotFound_shouldThrow() {
        assertThrows(NotFoundException.class, () -> tagsController.getLinksWithTag(new TagsRequest(777, "tag")));
    }

    @Test
    void getLinksWithTag_tagNotFound_shouldReturnEmptyList() {
        userRepository.save(new User(4, List.of(), List.of("tag")));
        var result = tagsController.getLinksWithTag(new TagsRequest(4, "qrwq"));
        assertEquals(0, result.size());
        assertTrue(result.links().isEmpty());
    }

    @Test
    void getLinksWithTag_linksFound_shouldReturnListOfAllLinks() {
        User u = new User(5, new ArrayList<>(), List.of());
        TrackedLink l1 = new TrackedLink(0, u, "a", "ms", List.of("tag"), new Filters(), "sid", 777);
        TrackedLink l2 = new TrackedLink(0, u, "aa", "ms", List.of("tag"), new Filters(), "sid", 777);
        TrackedLink l3 = new TrackedLink(0, u, "aaa", "ms", List.of("tag1"), new Filters(), "sid", 777);
        u.links().add(l1);
        u.links().add(l2);
        u.links().add(l3);
        userRepository.save(u);

        var result = tagsController.getLinksWithTag(new TagsRequest(5, "tag"));
        assertThat(result.links())
                .satisfiesExactlyInAnyOrder(l -> assertEquals("a", l.url()), l -> assertEquals("aa", l.url()));
    }
}
