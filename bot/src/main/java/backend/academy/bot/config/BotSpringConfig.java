package backend.academy.bot.config;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class BotSpringConfig {
    @Bean
    public TelegramBot telegramBot(BotConfig config) {
        return new TelegramBot(config.telegramToken());
    }

    @Bean
    public RestClient scrapperRestClient(BotConfig config) {
        return RestClient.builder().baseUrl(config.scrapperUrl()).build();
    }

    @Bean
    public WebClient scrapperWebClient(BotConfig config) {
        return WebClient.builder().baseUrl(config.scrapperUrl()).build();
    }
}
