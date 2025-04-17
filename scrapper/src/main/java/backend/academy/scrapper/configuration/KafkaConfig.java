package backend.academy.scrapper.configuration;

import backend.academy.api.model.LinkUpdate;
import lombok.experimental.UtilityClass;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.util.HashMap;
import java.util.Map;
import static backend.academy.scrapper.configuration.KafkaConfig.KafkaBeans.KAFKA_TEMPLATE_BEAN;

@Configuration
@EnableKafka
public class KafkaConfig {
    @Autowired
    private KafkaProperties kafkaProperties;

    @Autowired
    private ScrapperConfig scrapperConfig;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic updatesTopic() {
        return new NewTopic(scrapperConfig.kafkaTopics().updates(), 1, (short) 1);
    }

    @Bean
    public NewTopic deadLettersTopic() {
        return new NewTopic(scrapperConfig.kafkaTopics().deadLettersQueue(), 1, (short) 1);
    }


    @Bean(KAFKA_TEMPLATE_BEAN)
    public KafkaTemplate<String, Object> kafkaTemplate() {
        Map<String, Object> props = kafkaProperties.buildProducerProperties();
        props.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            kafkaProperties.getBootstrapServers());
        props.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            LongSerializer.class);
        props.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            JsonSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }


    @UtilityClass
    public static class KafkaBeans {
        public static final String KAFKA_TEMPLATE_BEAN = "kafkaTemplateBean";
    }
}
