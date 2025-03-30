package backend.academy.scrapper.repositories.orm;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.UserRepositoryTest;
import backend.academy.scrapper.repositories.impls.sql.SqlUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UserRepository.class, properties = "app.access-type=orm")
public class OrmUserRepositoryTest extends UserRepositoryTest {
    @Test
    public void linkRepository_useCorrectImpl() {
        assertThat(repository).isNotInstanceOf(SqlUserRepository.class);
    }
}
