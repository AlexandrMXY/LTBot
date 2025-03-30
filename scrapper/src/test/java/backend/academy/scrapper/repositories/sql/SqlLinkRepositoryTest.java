package backend.academy.scrapper.repositories.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.LinkRepositoryTest;
import backend.academy.scrapper.repositories.impls.sql.SqlLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = LinkRepository.class, properties = "app.db-access-impl=sql")
public class SqlLinkRepositoryTest extends LinkRepositoryTest {
    @Test
    public void linkRepository_useCorrectImpl() {
        assertThat(repository).isInstanceOf(SqlLinkRepository.class);
    }
}
