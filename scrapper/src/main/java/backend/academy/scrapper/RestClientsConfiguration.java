package backend.academy.scrapper;

import backend.academy.scrapper.util.MapBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientsConfiguration {
    @Bean
    public RestClient botRestClient(ScrapperConfig config) {
        return RestClient.builder().baseUrl(config.botUrl()).build();
    }

    @Bean
    public RestClient stackoverflowRestClient(ScrapperConfig config) {
        return RestClient.builder()
                .baseUrl(config.stackoverflowApiUrl())
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
                .baseUrl(config.githubApiUrl())
                .defaultHeader("Authorization", "Bearer " + config.githubToken())
                .build();
    }
}
