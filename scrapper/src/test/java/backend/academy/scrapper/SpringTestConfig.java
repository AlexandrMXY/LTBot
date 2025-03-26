package backend.academy.scrapper;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.UserRepository;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@TestConfiguration
@SpringBootApplication(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
        })
@Profile("!testDb")
public class SpringTestConfig {
    @Bean
    @Primary
    public NamedParameterJdbcTemplate jdbcTemplate() {
        return Mockito.mock(NamedParameterJdbcTemplate.class);
    }

    @Bean
    @Primary
    public UserRepository userRepository() {
        return Mockito.mock(UserRepository.class);
    }

    @Bean
    @Primary
    public LinkRepository linkRepository() {
        return Mockito.mock(LinkRepository.class);
    }

    @Bean
    @Primary
    public ScrapperConfig scrapperConfig() {
        return new ScrapperConfig(
                "A",
                new ScrapperConfig.StackOverflowCredentials("A", "A"),
                ".*",
                "http://localhost:8080",
                "http://localhost:8082",
                "http://localhost:8083",
            ScrapperConfig.DBAccessImpl.ORM);
    }
}
