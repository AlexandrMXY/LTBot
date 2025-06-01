package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.configuration.KafkaConfig;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.kafka.core.KafkaTemplate;

@CircuitBreaker(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
@TimeLimiter(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
@Retry(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@AllArgsConstructor
@Slf4j
public class KafkaBotNotificationSender implements BotNotificationSender {
    public static final String RESILIENCE4J_INSTANCE_NAME = "kafka-notification-sender";

    private KafkaTemplate kafkaTemplate;
    private ScrapperConfig scrapperConfig;
    private HttpBotNotificationSender fallbackNotificationSender;

    @Override
    @CircuitBreaker(name = RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
    @Retry(name = RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
    @TimeLimiter(name = RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
    public void sendUpdates(Updates updates) {
        sendUpdatesWithoutFallback(updates);
    }

    @CircuitBreaker(name = RESILIENCE4J_INSTANCE_NAME)
    @Retry(name = RESILIENCE4J_INSTANCE_NAME)
    @TimeLimiter(name = RESILIENCE4J_INSTANCE_NAME)
    public void sendUpdatesWithoutFallback(Updates updates) {
        if (updates == null) return;

        Update update;
        while ((update = updates.peek()) != null) {
            var request = update.createRequest();
            kafkaTemplate.send(scrapperConfig.kafkaTopics().updates(), request).join();
            updates.pop();
        }
    }

    @SuppressWarnings("unused")
    private void fallback(Updates updates, RuntimeException exception) {
        log.atWarn()
                .setCause(exception)
                .log("Failed to send updates with KafkaBotNotificationSender. Using fallback notification sender");
        if (fallbackNotificationSender == null) {
            throw exception;
        }
        fallbackNotificationSender.sendUpdatesWithoutFallback(updates);
    }

    @Configuration
    public static class KafkaBotNotificationSenderConfiguration {
        @Bean
        @ConditionalOnBean(KafkaConfig.class)
        @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
        public KafkaBotNotificationSender kafkaBotNotificationSender(
                @Qualifier(KafkaConfig.KafkaBeans.KAFKA_TEMPLATE_BEAN) KafkaTemplate kafkaTemplate,
                ScrapperConfig scrapperConfig,
                @Lazy @Autowired(required = false) HttpBotNotificationSender fallbackNotificationSender) {
            return new KafkaBotNotificationSender(kafkaTemplate, scrapperConfig, fallbackNotificationSender);
        }
    }
}
