package backend.academy.scrapper.repositories;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.AbstractAppTest;
import backend.academy.scrapper.entities.TrackedLink;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.entities.filters.Filters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@Slf4j
public abstract class LinkRepositoryTest extends AbstractAppTest {
    @Autowired
    protected LinkRepository repository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TestEntityManager entityManager;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    @Transactional
    public void save_newLink_savedSuccessfully() {
        TrackedLink link = new TrackedLink(
                0, new User(0, new ArrayList<>()), "https://localhost", "AAA", List.of(), new Filters(), "1010", 10L);
        link.user().links().add(link);

        TrackedLink saved = repository.save(link);

        assertEquals(saved, entityManager.find(TrackedLink.class, saved.id()));
    }

    @Test
    @Transactional
    public void save_addAnotherLinkToUser_savedSuccessfully() {
        User u = new User(1, new ArrayList<>());
        TrackedLink link = new TrackedLink(0, u, "https://localhost", "AAA", List.of(), new Filters(), "1010", 10L);
        u.links().add(link);

        entityManager.persist(u);
        entityManager.persistAndFlush(link);

        TrackedLink link2 = new TrackedLink(0, u, "https://localhost:2", "BBB", List.of(), new Filters(), "01", 100L);
        u.links().add(link2);

        TrackedLink saved = repository.save(link2);

        assertEquals(link2, entityManager.find(TrackedLink.class, saved.id()));
    }

    @Test
    @Transactional
    public void existsByUserAndMonitoringServiceAndServiceId_exists_returnTrue() {
        User u = new User(1, new ArrayList<>());
        TrackedLink link = new TrackedLink(0, u, "https://localhost", "AAA", List.of(), new Filters(), "1010", 10L);
        u.links().add(link);

        entityManager.persist(u);
        entityManager.persistAndFlush(link);

        assertTrue(repository.existsByUserAndMonitoringServiceAndServiceId(u, "AAA", "1010"));
    }

    @Test
    public void existsByUserAndMonitoringServiceAndServiceId_dontExists_returnFalse() {
        assertFalse(repository.existsByUserAndMonitoringServiceAndServiceId(new User(44, List.of()), "AAA", "1010"));
    }

    @Test
    @Transactional()
    public void deleteById_whenCalled_shouldDelete() {
        User u = new User(1, new ArrayList<>());
        TrackedLink link = new TrackedLink(0, u, "https://localhost", "AAA", List.of(), new Filters(), "1010", 10L);
        u.links().add(link);

        entityManager.persist(u);
        TrackedLink saved = entityManager.persistAndFlush(link);

        entityManager.clear();

        u.links().clear();
        userRepository.save(u);
        repository.deleteById(saved.id());

        assertNull(entityManager.find(TrackedLink.class, saved.id()));
    }

    @Test
    @Transactional
    public void findAllByMonitoringServiceAndLastUpdateLessThanOrderById_whenCalled_shouldReturnCorrectlySorted() {
        User u1 = new User(5, new ArrayList<>());
        User u2 = new User(6, new ArrayList<>());
        addLinkToUser(u1, "a", 100);
        addLinkToUser(u2, "b", 110);
        addLinkToUser(u2, "a", 120);
        addLinkToUser(u1, "a", 130);
        addLinkToUser(u2, "b", 140);
        addLinkToUser(u2, "b", 140);
        addLinkToUser(u1, "a", 150);
        entityManager.persist(u1);
        entityManager.persist(u2);
        entityManager.flush();

        Page<TrackedLink> res =
                repository.findAllByMonitoringServiceAndLastUpdateLessThanOrderById("a", 145L, Pageable.ofSize(10));

        assertThatIterable(res.toList()).allSatisfy((l) -> {
            assertEquals("a", l.monitoringService());
            assertThat(l.lastUpdate()).isLessThan(145);
        });

        var resList = res.toList();
        for (int i = 1; i < resList.size(); i++) {
            assertThat(resList.get(i - 1).id()).isLessThan(resList.get(i).id());
        }
    }

    @Test
    @Transactional
    public void findAllByMonitoringServiceAndLastUpdateLessThanOrderById_whenCalled_paginatedCorrectly() {
        User u = new User(7, new ArrayList<>());
        addLinkToUser(u, "a", 100);
        addLinkToUser(u, "a", 110);
        addLinkToUser(u, "a", 120);
        addLinkToUser(u, "a", 130);
        addLinkToUser(u, "a", 140);
        addLinkToUser(u, "a", 140);
        addLinkToUser(u, "a", 150);
        entityManager.persist(u);
        entityManager.flush();

        Pageable pageable = Pageable.ofSize(2);

        List<List<TrackedLink>> result = new ArrayList<>();

        Page<TrackedLink> page;
        do {
            page = repository.findAllByMonitoringServiceAndLastUpdateLessThanOrderById("a", 200, pageable);
            result.add(page.toList());
            pageable = pageable.next();
        } while (pageable.getPageNumber() <= page.getTotalPages());

        for (int i = 0; i < 3; i++) assertEquals(2, result.get(i).size());
        assertEquals(1, result.get(3).size());

        List<TrackedLink> flatRes = result.stream().flatMap(Collection::stream).toList();

        for (int i = 1; i < flatRes.size(); i++)
            assertThat(flatRes.get(i - 1).id()).isLessThan(flatRes.get(i).id());
    }

    @Test
    @Transactional
    public void findAllByUserId_whenCalled_returnAllLinks() {
        User u = new User(8, new ArrayList<>());
        addLinkToUser(u, "a");
        addLinkToUser(u, "aa");
        addLinkToUser(u, "aaa");
        addLinkToUser(u, "aaaa");
        entityManager.persistAndFlush(u);

        assertThat(repository.findAllByUserId(u.id()))
                .satisfiesExactlyInAnyOrder(
                        (l) -> {
                            assertEquals(u.id(), l.user().id());
                            assertEquals("a", l.url());
                        },
                        l -> {
                            assertEquals(u.id(), l.user().id());
                            assertEquals("aa", l.url());
                        },
                        l -> {
                            assertEquals(u.id(), l.user().id());
                            assertEquals("aaa", l.url());
                        },
                        l -> {
                            assertEquals(u.id(), l.user().id());
                            assertEquals("aaaa", l.url());
                        });
    }

    @Test
    @Transactional
    public void findAllByUserId_whenNoLinks_returnEmptyList() {
        User u = new User(9, new ArrayList<>());
        entityManager.persistAndFlush(u);

        assertThat(repository.findAllByUserId(u.id())).isEmpty();
    }

    @Test
    @Transactional
    public void findByUserIdAndUrl_whenLinkExists_returnLink() {
        User u = new User(10, new ArrayList<>());
        addLinkToUser(u, "a");
        addLinkToUser(u, "aa");
        addLinkToUser(u, "aaa");
        entityManager.persistAndFlush(u);

        assertThat(repository.findByUserIdAndUrl(u.id(), "aa"))
                .isNotEmpty()
                .get()
                .satisfies(l -> {
                    assertEquals(u.id(), l.user().id());
                    assertEquals("aa", l.url());
                });
    }

    @Test
    @Transactional
    public void findByUserIdAndUrl_whenLinkNotExists_returnLink() {
        User u = new User(11, new ArrayList<>());
        addLinkToUser(u, "a");
        addLinkToUser(u, "aa");
        addLinkToUser(u, "aaa");
        entityManager.persistAndFlush(u);

        assertThat(repository.findByUserIdAndUrl(u.id(), "aaaa")).isEmpty();
    }

    private void addLinkToUser(User u, String monitor, long lastUpdate) {
        TrackedLink link =
                new TrackedLink(0, u, "https://localhost", monitor, List.of(), new Filters(), "1010", lastUpdate);
        u.links().add(link);
        entityManager.persist(link);
    }

    private void addLinkToUser(User u, String url) {
        TrackedLink link = new TrackedLink(0, u, url, "monitor", List.of(), new Filters(), "1010", 10L);
        u.links().add(link);
        entityManager.persist(link);
    }
}
