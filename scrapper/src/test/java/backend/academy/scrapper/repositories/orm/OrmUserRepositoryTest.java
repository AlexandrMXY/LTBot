package backend.academy.scrapper.repositories.orm;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.UserRepositoryTest;
import backend.academy.scrapper.repositories.impls.sql.SqlLinkRepository;
import backend.academy.scrapper.repositories.impls.sql.SqlUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = UserRepository.class, properties = "app.db-access-impl=orm")
public class OrmUserRepositoryTest extends UserRepositoryTest {
    @Test
    public void linkRepository_useCorrectImpl() {
        assertThat(repository).isNotInstanceOf(SqlUserRepository.class);
    }
}
