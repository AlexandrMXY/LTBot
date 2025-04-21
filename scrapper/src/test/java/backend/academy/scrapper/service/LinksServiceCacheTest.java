package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.AbstractAppTest;
import backend.academy.scrapper.dto.LinkDto;
import backend.academy.scrapper.entities.User;
import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
public class LinksServiceCacheTest extends AbstractAppTest {
    public static final String VALID_LINK = "https://stackoverflow.com/questions/99999999/q";
    public static final String VALID_LINK_2 = "https://stackoverflow.com/questions/99999998/qq";

    @Autowired
    private LinksService linksService;

    @MockitoSpyBean
    private LinkRepository linkRepository;

    @MockitoSpyBean
    private UserRepository userRepository;

    @Test
    public void getLinks_ifNoUpdates_returnCachedValue() {
        userRepository.save(new User(200));
        linksService.getLinks(200);
        clearInvocations(linkRepository, userRepository);
        verify(userRepository, never()).findById(anyLong());
        verify(linkRepository, never()).findAllByUserId(anyLong());
        linksService.getLinks(200);
    }

    @Test
    public void getLinks_updated_returnDatabaseValue() {
        userRepository.save(new User(201));
        linksService.addLink(201, new LinkDto(VALID_LINK, List.of(), List.of(), 0));
        clearInvocations(linkRepository, userRepository);
        linksService.getLinks(201);
        verify(userRepository, times(1)).findById(eq(201L));
    }

    @Test
    public void getLinks_updated_returnCorrectValue() {
        userRepository.save(new User(202));
        linksService.addLink(202, new LinkDto(VALID_LINK, List.of("tag1", "tag2"), List.of(), 0));
        linksService.addLink(202, new LinkDto(VALID_LINK_2, List.of("tag2", "tag3"), List.of(), 0));
        List<LinkDto> result = linksService.getLinks(202);
        assertThat(result)
                .satisfiesExactlyInAnyOrder(
                        e -> {
                            assertThat(e.link()).isEqualTo(VALID_LINK);
                            assertThat(e.tags()).containsExactlyInAnyOrderElementsOf(List.of("tag1", "tag2"));
                            assertThat(e.filters()).isEmpty();
                        },
                        e -> {
                            assertThat(e.link()).isEqualTo(VALID_LINK_2);
                            assertThat(e.tags()).containsExactlyInAnyOrderElementsOf(List.of("tag2", "tag3"));
                            assertThat(e.filters()).isEmpty();
                        });
    }
}
