package backend.academy.scrapper.repositories.sql;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.UserRepositoryTest;
import backend.academy.scrapper.repositories.impls.sql.SqlUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserRepository.class, properties = "app.db-access-impl=sql")
public class SqlUserRepositoryTest extends UserRepositoryTest {
    @Test
    public void linkRepository_useCorrectImpl() {
        assertThat(repository).isInstanceOf(SqlUserRepository.class);
    }
}
