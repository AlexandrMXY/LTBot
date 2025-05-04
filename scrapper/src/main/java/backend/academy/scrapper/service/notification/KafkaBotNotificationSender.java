package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.configuration.KafkaConfig;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
//@ConditionalOnBean(KafkaConfig.class)
@CircuitBreaker(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
@TimeLimiter(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
@Retry(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class KafkaBotNotificationSender implements BotNotificationSender {
    public static final String RESILIENCE4J_INSTANCE_NAME = "kafka-notification-sender";

    @Autowired
    @Qualifier(KafkaConfig.KafkaBeans.KAFKA_TEMPLATE_BEAN)
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private ScrapperConfig scrapperConfig;

    @Lazy
    @Autowired(required = false)
    private HttpBotNotificationSender fallbackNotificationSender;

    @Override
    @CircuitBreaker(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
    @Retry(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
    @TimeLimiter(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME, fallbackMethod = "fallback")
    public void sendUpdates(Updates updates) {
        sendUpdatesWithoutFallback(updates);
    }

    @CircuitBreaker(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
    @Retry(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
    @TimeLimiter(name = KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
    public void sendUpdatesWithoutFallback(Updates updates) {
        if (updates == null) return;
        for (Update update : updates.getUpdates()) {
            var request = update.createRequest();
            kafkaTemplate.send(scrapperConfig.kafkaTopics().updates(), request).join();
        }
    }

    @SuppressWarnings("unused")
    private void fallback(Updates updates, RuntimeException exception) {
        log.info("Failed to send updates with KafkaBotNotificationSender. Using fallback notification sender");
        if (fallbackNotificationSender == null) {
            throw exception;
        }
        fallbackNotificationSender.sendUpdatesWithoutFallback(updates);
    }
}
