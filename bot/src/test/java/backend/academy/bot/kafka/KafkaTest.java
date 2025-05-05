package backend.academy.bot.kafka;

import static org.mockito.Mockito.*;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.AbstractKafkaTest;
import backend.academy.bot.config.BotConfig;
import backend.academy.bot.service.UpdatesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
public class KafkaTest extends AbstractKafkaTest {
    @Autowired
    private BotConfig botConfig;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoSpyBean
    private UpdatesService updatesService;

    @Test
    public void onInvalidUpdateMessage_movedToDLQ() throws InterruptedException {
        byte[] bytes = "aaa".getBytes();
        testTemplate.send(botConfig.kafkaTopics().updates(), bytes);

        var record = DLQ_RECORDS.poll(100, TimeUnit.SECONDS);

        Assertions.assertThat(record.value()).containsExactly(bytes);
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    public synchronized void onValidMessage_updateProcessed() throws JsonProcessingException, InterruptedException {
        LinkUpdate update = new LinkUpdate(0, 10L, "urlHere", "content", "user", "type");
        String content = objectMapper.writeValueAsString(update);

        CountDownLatch sync = new CountDownLatch(1); // Thread.currentThread();

        doAnswer(invocation -> {
                    sync.countDown();
                    return null;
                })
                .when(updatesService)
                .processUpdate(eq(update));

        testTemplate.send(botConfig.kafkaTopics().updates(), content.getBytes());

        sync.await();
    }
}
