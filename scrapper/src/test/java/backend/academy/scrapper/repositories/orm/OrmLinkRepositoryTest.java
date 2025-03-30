package backend.academy.scrapper.repositories.orm;

import static org.assertj.core.api.Assertions.*;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.LinkRepositoryTest;
import backend.academy.scrapper.repositories.impls.sql.SqlLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LinkRepository.class, properties = "app.access-type=orm")
public class OrmLinkRepositoryTest extends LinkRepositoryTest {
    @Test
    public void linkRepository_useCorrectImpl() {
        assertThat(repository).isNotInstanceOf(SqlLinkRepository.class);
    }
}
