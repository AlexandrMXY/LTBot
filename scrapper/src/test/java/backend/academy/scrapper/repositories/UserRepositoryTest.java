package backend.academy.scrapper.repositories;

import backend.academy.scrapper.SpringDBTestConfig;
import backend.academy.scrapper.entities.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
@Import({SpringDBTestConfig.class})
@Testcontainers
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureDataJpa
@ActiveProfiles("testDb")
public abstract class UserRepositoryTest {
    @Autowired
    UserRepository repository;

    @Autowired
    TestEntityManager entityManager;


    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres
        = new PostgreSQLContainer<>("postgres:16-alpine");


    @Test
    @Transactional
    void save_whenCalled_shouldSave() {
        User u = new User(0, new ArrayList<>());
        User saved = repository.save(u);
        assertEquals(saved, entityManager.find(User.class,  saved.id()));
    }

    @Test
    @Transactional
    void existsById_whenExists_shouldReturnTrue() {
        User u = new User(1, new ArrayList<>());
        entityManager.persistAndFlush(u);
        assertTrue(repository.existsById(u.id()));
    }

    @Test
    void existsById_whenNotExists_shouldReturnFalse() {
        assertFalse(repository.existsById(2));
    }

    @Test
    @Transactional
    void deleteById_whenCalled_shouldDelete() {
        User u = new User(3, new ArrayList<>());
        entityManager.persistAndFlush(u);
        repository.deleteById(u.id());
        assertFalse(repository.findById(u.id()).isPresent());
    }

    @Test
    @Transactional
    void findById_whenExists_shouldReturn() {
        User u = new User(4, new ArrayList<>());
        entityManager.persistAndFlush(u);
        assertEquals(u, repository.findById(u.id()).orElseThrow());
    }

    @Test
    void findById_whenNotExists_shouldReturnEmptyOpt() {
        assertFalse(repository.findById(5).isPresent());
    }
}
