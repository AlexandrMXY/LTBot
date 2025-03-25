package backend.academy.scrapper;

import backend.academy.scrapper.repositories.LinkRepository;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;


@SpringBootApplication
@EnableJpaRepositories
@TestConfiguration
@Profile("testDb")
public class SpringDBTestConfig {
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
