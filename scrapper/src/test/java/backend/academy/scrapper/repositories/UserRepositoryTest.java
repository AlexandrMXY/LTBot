package backend.academy.scrapper.repositories;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.AbstractAppTest;
import backend.academy.scrapper.entities.User;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public abstract class UserRepositoryTest extends AbstractAppTest {
    @Autowired
    protected UserRepository repository;

    @Autowired
    TestEntityManager entityManager;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    @Transactional
    void save_whenCalled_shouldSave() {
        User u = new User(0, new ArrayList<>());
        User saved = repository.save(u);
        assertEquals(saved, entityManager.find(User.class, saved.id()));
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
