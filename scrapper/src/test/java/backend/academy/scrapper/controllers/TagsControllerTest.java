package backend.academy.scrapper.controllers;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.api.exceptions.NotFoundException;
import backend.academy.api.model.TagsRequest;
import backend.academy.scrapper.SpringDBTestConfig;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest()
@Import({SpringDBTestConfig.class})
@Testcontainers
@AutoConfigureTestDatabase
@ActiveProfiles("testDb")
class TagsControllerTest {
    @Autowired
    TagsController tagsController;

    @Autowired
    UserRepository userRepository;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

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
}
