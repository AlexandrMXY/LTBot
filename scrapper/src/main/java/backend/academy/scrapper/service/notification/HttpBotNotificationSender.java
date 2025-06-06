package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.web.clients.BotRestClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HttpBotNotificationSender implements BotNotificationSender {
    public static final String RESILIENCE4J_INSTANCE_NAME = "http-notification-sender";

    @Autowired
    private BotRestClient client;

    @Lazy
    @Autowired(required = false)
    private KafkaBotNotificationSender fallbackNotificationSender;

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
            client.postRequest("/updates", request);
            updates.pop();
        }
    }

    @SuppressWarnings("unused")
    private void fallback(Updates updates, RuntimeException exception) {
        log.atWarn()
                .setCause(exception)
                .log("Failed to send updates with HttpBotNotificationSender. " + (fallbackNotificationSender == null ? "" : "Using fallback notification sender"));
        if (fallbackNotificationSender == null) {
            throw exception;
        }
        fallbackNotificationSender.sendUpdatesWithoutFallback(updates);
    }
}
