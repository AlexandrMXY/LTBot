package backend.academy.scrapper;

import backend.academy.scrapper.repositories.LinkRepository;
import backend.academy.scrapper.repositories.MonitoringServiceDataRepository;
import backend.academy.scrapper.repositories.UserRepository;
import backend.academy.scrapper.util.MapBuilder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@TestConfiguration
@SpringBootApplication(
        exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
        })
public class SpringTestConfig {
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
    public MonitoringServiceDataRepository monitoringServiceDataRepository() {
        return Mockito.mock(MonitoringServiceDataRepository.class);
    }

    @Bean
    @Primary
    public ScrapperConfig scrapperConfig() {
        return new ScrapperConfig("GT", new ScrapperConfig.StackOverflowCredentials("SOK", "SOT"), ".*");
    }

    @Bean
    public RestClient botRestClient(@Value("${app.bot-url}") String botUrl, MockClientsHolder holder) {
        var builder = RestClient.builder().baseUrl(botUrl);
        holder.bot = MockRestServiceServer.bindTo(builder).build();
        return builder.build();
    }

    @Bean
    public RestClient stackoverflowRestClient(ScrapperConfig config, MockClientsHolder holder) {
        var builder = RestClient.builder()
                .baseUrl("https://api.stackexchange.com/2.2")
                .defaultUriVariables(MapBuilder.<String, String>builder()
                        .put("key", config.stackOverflow().key())
                        .put("access_token", config.stackOverflow().accessToken())
                        .put("site", "stackoverflow")
                        .build());
        holder.stackoverflow = MockRestServiceServer.bindTo(builder).build();
        return builder.build();
    }

    @Bean
    RestClient githubRestClient(ScrapperConfig config, MockClientsHolder holder) {
        var builder = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + config.githubToken());
        holder.github = MockRestServiceServer.bindTo(builder).build();
        return builder.build();
    }
}
