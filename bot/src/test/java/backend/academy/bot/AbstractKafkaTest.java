package backend.academy.bot;

import static org.apache.kafka.clients.consumer.ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;

import backend.academy.bot.config.BotConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

@Import(AbstractKafkaTest.KafkaTestConfig.class)
public abstract class AbstractKafkaTest extends AbstractAppTest {
    @Autowired
    @Qualifier("testKafkaTemplate")
    protected KafkaTemplate<Long, byte[]> testTemplate;

    protected static final BlockingQueue<ConsumerRecord<Long, byte[]>> DLQ_RECORDS = new LinkedBlockingQueue<>(1);

    @Autowired
    @Qualifier("dlqConsumer")
    protected KafkaMessageListenerContainer<Long, byte[]> dlqConsumer;

    @TestConfiguration
    public static class KafkaTestConfig {
        @Bean("testKafkaTemplate")
        public KafkaTemplate<Long, byte[]> testTemplate(KafkaProperties kafkaProperties) {
            Map<String, Object> props = new KafkaProperties().buildProducerProperties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
            return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
        }

        @Bean("dlqConsumer")
        KafkaMessageListenerContainer<Long, byte[]> dlqConsumer(
                KafkaProperties kafkaProperties,
                BotConfig botConfig,
                @Qualifier("testConsumerFactory") DefaultKafkaConsumerFactory<Long, byte[]> consumerFactor) {
            var clientConsumerContainerProperties =
                    new ContainerProperties(botConfig.kafkaTopics().deadLettersQueue());
            clientConsumerContainerProperties.setAckMode(ContainerProperties.AckMode.MANUAL);

            var dlqConsumer = new KafkaMessageListenerContainer<>(consumerFactor, clientConsumerContainerProperties);
            dlqConsumer.setupMessageListener((AcknowledgingMessageListener<Long, byte[]>) (data, acknowledgment) -> {
                try {
                    DLQ_RECORDS.put(data);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Objects.requireNonNull(acknowledgment).acknowledge();
            });
            dlqConsumer.start();
            return dlqConsumer;
        }

        @Bean("testConsumerFactory")
        public DefaultKafkaConsumerFactory<Long, byte[]> testConsumerFactory(KafkaProperties kafkaProperties) {
            final var clientConsumerProps = new HashMap<String, Object>();
            clientConsumerProps.put(BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
            clientConsumerProps.put(GROUP_ID_CONFIG, "consumer");
            clientConsumerProps.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
            clientConsumerProps.put(ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
            clientConsumerProps.put(MAX_POLL_RECORDS_CONFIG, "1");
            clientConsumerProps.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
            clientConsumerProps.put(MAX_POLL_INTERVAL_MS_CONFIG, "15000");

            return new DefaultKafkaConsumerFactory<>(
                    clientConsumerProps, new LongDeserializer(), new ByteArrayDeserializer());
        }
    }
}
