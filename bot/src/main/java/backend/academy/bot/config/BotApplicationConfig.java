package backend.academy.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.policy.CircuitBreakerRetryPolicy;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableRetry
public class BotApplicationConfig {
    @Bean
    public TelegramBot telegramBot(BotConfig config) {
        return new TelegramBot(config.telegramToken());
    }

    @Bean
    public WebClient scrapperWebClient(BotConfig config) {
        return WebClient.builder()
            .baseUrl(config.scrapperUrl()).build();
    }
}
