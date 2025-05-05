package backend.academy.bot.config;

import static backend.academy.bot.config.KafkaConfig.KafkaBeans.DEAD_LETTERS_TEMPLATE;
import static backend.academy.bot.config.KafkaConfig.KafkaBeans.DEFAULT_CONSUMER_FACTORY;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
@EnableKafka
@Profile("!noKafka")
public class KafkaConfig {
    @Autowired
    private BotConfig botConfig;

    @Autowired
    private KafkaProperties properties;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic updatesTopic() {
        return new NewTopic(botConfig.kafkaTopics().updates(), 1, (short) 1);
    }

    @Bean
    public NewTopic deadLettersTopic() {
        return new NewTopic(botConfig.kafkaTopics().deadLettersQueue(), 1, (short) 1);
    }

    @Bean(DEAD_LETTERS_TEMPLATE)
    public KafkaTemplate<Long, Object> deadLettersTopicKafkaTemplate() {
        Map<String, Object> props = properties.buildProducerProperties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean(DEFAULT_CONSUMER_FACTORY)
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Long, byte[]>> defaultConsumerFactory() {
        var consumerFactoryProps = properties.buildConsumerProperties(null);
        consumerFactoryProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        consumerFactoryProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        consumerFactoryProps.put(
                ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
        consumerFactoryProps.put(ConsumerConfig.GROUP_ID_CONFIG, "default-consumer");
        var consumerFactory = new DefaultKafkaConsumerFactory<>(consumerFactoryProps);

        var factory = new ConcurrentKafkaListenerContainerFactory<Long, byte[]>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(new CommonLoggingErrorHandler());
        factory.setAutoStartup(true);
        factory.setConcurrency(1);
        factory.getContainerProperties().setPollTimeout(1000);
        return factory;
    }

    @UtilityClass
    public static class KafkaBeans {
        public static final String DEAD_LETTERS_TEMPLATE = "deadLettersKafkaTemplate";

        public static final String DEFAULT_CONSUMER_FACTORY = "defaultConsumerFactory";
    }
}
