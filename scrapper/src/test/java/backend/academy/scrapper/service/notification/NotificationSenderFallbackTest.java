package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.configuration.KafkaConfig;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.updates.Updates;
import backend.academy.scrapper.web.clients.BotRestClient;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import java.util.stream.Stream;

@SpringBootTest(classes = {KafkaBotNotificationSender.class, HttpBotNotificationSender.class})
@Import({CircuitBreakerAutoConfiguration.class})
@EnableRetry
public class NotificationSenderFallbackTest {
    @MockitoBean
    private BotRestClient botRestClient;
    @MockitoBean(name = KafkaConfig.KafkaBeans.KAFKA_TEMPLATE_BEAN)
    private KafkaTemplate kafkaTemplate;
    @MockitoBean
    private ScrapperConfig scrapperConfig;

    @MockitoSpyBean
    private KafkaBotNotificationSender kafkaBotNotificationSender;
    @MockitoSpyBean
    private HttpBotNotificationSender httpBotNotificationSender;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;


    @BeforeEach
    public void initMocks() {
        reset(httpBotNotificationSender, kafkaBotNotificationSender);
    }


    @Test
    public void kafkaSender_sendUpdates_onCircuitBreakerOpen_useFallbackSender() {
        circuitBreakerRegistry.circuitBreaker(KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
            .transitionToForcedOpenState();
        circuitBreakerRegistry.circuitBreaker(HttpBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
            .transitionToDisabledState();
        try {
            kafkaBotNotificationSender.sendUpdates(new Updates());
        } catch (Throwable throwable) {}
        verify(httpBotNotificationSender, times(1))
            .sendUpdatesWithoutFallback(any());
    }

    @Test
    public void httpSender_sendUpdates_onCircuitBreakerOpen_useFallbackSender() {
        circuitBreakerRegistry.circuitBreaker(HttpBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
            .transitionToForcedOpenState();
        circuitBreakerRegistry.circuitBreaker(KafkaBotNotificationSender.RESILIENCE4J_INSTANCE_NAME)
            .transitionToDisabledState();
        try {
            httpBotNotificationSender.sendUpdates(new Updates());
        } catch (Throwable throwable) {}
        verify(kafkaBotNotificationSender, times(1))
            .sendUpdatesWithoutFallback(any());
    }

}
