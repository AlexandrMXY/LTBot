package backend.academy.scrapper.repositories.orm;


import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.LinkRepositoryTest;
import backend.academy.scrapper.repositories.impls.sql.SqlLinkRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest(classes = LinkRepository.class, properties = "app.db-access-impl=orm")
public class OrmLinkRepositoryTest extends LinkRepositoryTest {
    @Test
    public void linkRepository_useCorrectImpl() {
        assertThat(repository).isNotInstanceOf(SqlLinkRepository.class);
    }
}
