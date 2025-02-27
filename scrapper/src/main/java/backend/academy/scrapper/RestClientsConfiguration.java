package backend.academy.scrapper;

import backend.academy.scrapper.util.MapBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("prod")
public class RestClientsConfiguration {
    @Bean
    public RestClient botRestClient(@Value("${app.bot-url}") String botUrl) {
        return RestClient.builder().baseUrl(botUrl).build();
    }

    @Bean
    public RestClient stackoverflowRestClient(ScrapperConfig config) {
        return RestClient.builder()
                .baseUrl("https://api.stackexchange.com/2.2")
                .defaultUriVariables(MapBuilder.<String, String>builder()
                        .put("key", config.stackOverflow().key())
                        .put("access_token", config.stackOverflow().accessToken())
                        .put("site", "stackoverflow")
                        .build())
                .build();
    }

    @Bean
    RestClient githubRestClient(ScrapperConfig config) {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + config.githubToken())
                .build();
    }
}
