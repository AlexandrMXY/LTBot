package backend.academy.scrapper.repositories.orm;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.repositories.UserRepositoryTest;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = UserRepository.class, properties = "app.db-access-impl=orm")
public class OrmUserRepositoryTest extends UserRepositoryTest {
}
