package backend.academy.scrapper.repositories.sql;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.LinkRepositoryTest;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest(classes = LinkRepository.class, properties = "app.db-access-impl=sql")
public class SqlLinkRepositoryTest extends LinkRepositoryTest {
}
