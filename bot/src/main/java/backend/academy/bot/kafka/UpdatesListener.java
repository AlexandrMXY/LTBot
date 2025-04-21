package backend.academy.bot.kafka;

import backend.academy.api.model.LinkUpdate;
import backend.academy.bot.config.BotConfig;
import backend.academy.bot.config.KafkaConfig;
import backend.academy.bot.service.UpdatesService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@Profile("!noKafka")
public class UpdatesListener {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier(KafkaConfig.KafkaBeans.DEAD_LETTERS_TEMPLATE)
    private KafkaTemplate deadLetterTemplate;

    @Autowired
    private BotConfig botConfig;

    @Autowired
    private UpdatesService updatesService;

    @KafkaListener(
            containerFactory = KafkaConfig.KafkaBeans.DEFAULT_CONSUMER_FACTORY,
            topicPartitions =
                    @TopicPartition(
                            topic = "${app.kafka-topics.updates}",
                            partitions = {"0"}))
    public void consume(ConsumerRecord<Long, byte[]> data, Acknowledgment acknowledgment) {
        try {
            var update = objectMapper.readValue(data.value(), LinkUpdate.class);
            updatesService.processUpdate(update);
        } catch (Throwable e) {
            log.atWarn()
                    .setMessage("Kafka message processing failed")
                    .setCause(e)
                    .log();
            deadLetterTemplate.send(botConfig.kafkaTopics().deadLettersQueue(), data.key(), data.value());
        }
        acknowledgment.acknowledge();
    }
}
