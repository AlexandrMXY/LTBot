package backend.academy.scrapper.configuration;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.experimental.UtilityClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
@EnableScheduling
public record ScrapperConfig(
        @NotEmpty String githubToken,
        StackOverflowCredentials stackOverflow,
        @NotEmpty String tagsFilterRegex,
        @NotEmpty String botUrl,
        @NotEmpty String githubApiUrl,
        @NotEmpty String stackoverflowApiUrl,
        DBAccessImpl accessType,
        MessageTransport messageTransport,
        @Min(1) int updateThreadsCnt,
        @Min(1) int updateThreadBatchSize,
        KafkaTopics kafkaTopics) {

    public record StackOverflowCredentials(@NotEmpty String key, @NotEmpty String accessToken) {}

    public enum DBAccessImpl {
        SQL,
        ORM
    }
    public enum MessageTransport {
        KAFKA,
        HTTP
    }

    public record KafkaTopics (
        @NotEmpty String updates,
        @NotEmpty String deadLettersQueue
    ) { }
}
