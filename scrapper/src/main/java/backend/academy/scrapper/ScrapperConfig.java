package backend.academy.scrapper;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app", ignoreUnknownFields = true)
@EnableScheduling
public record ScrapperConfig(
        @NotEmpty String githubToken, StackOverflowCredentials stackOverflow, String tagsFilterRegex) {

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}
}
