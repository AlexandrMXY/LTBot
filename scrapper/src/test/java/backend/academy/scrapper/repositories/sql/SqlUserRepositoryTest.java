package backend.academy.scrapper.repositories.sql;

import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.UserRepositoryTest;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = UserRepository.class, properties = "app.db-access-impl=sql")
public class SqlUserRepositoryTest extends UserRepositoryTest {
}
