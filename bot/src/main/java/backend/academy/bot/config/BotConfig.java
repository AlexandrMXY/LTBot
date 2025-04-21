package backend.academy.bot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record BotConfig(
        @NotEmpty String telegramToken,
        @NotEmpty String scrapperUrl,
        @Min(1000) long sessionTimeout,
        KafkaTopics kafkaTopics) {

    public record KafkaTopics(@NotEmpty String updates, @NotEmpty String deadLettersQueue) {}
}
