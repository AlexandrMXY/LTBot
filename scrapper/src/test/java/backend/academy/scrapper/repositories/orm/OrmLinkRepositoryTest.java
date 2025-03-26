package backend.academy.scrapper.repositories.orm;


import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.LinkRepositoryTest;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = LinkRepository.class, properties = "app.db-access-impl=orm")
public class OrmLinkRepositoryTest extends LinkRepositoryTest {
}
