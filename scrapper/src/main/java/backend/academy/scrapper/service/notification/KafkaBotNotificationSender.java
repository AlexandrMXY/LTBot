package backend.academy.scrapper.service.notification;

import backend.academy.scrapper.configuration.KafkaConfig;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.updates.Update;
import backend.academy.scrapper.dto.updates.Updates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "app", name = "message-transport", havingValue = "kafka")
public class KafkaBotNotificationSender implements BotNotificationSender {
    @Autowired
    @Qualifier(KafkaConfig.KafkaBeans.KAFKA_TEMPLATE_BEAN)
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private ScrapperConfig scrapperConfig;

    @Override
    public void sendUpdates(Updates updates) {
        if (updates == null)
            return;
        for (Update update : updates.getUpdates()) {
            var request = update.createRequest();
            var result = kafkaTemplate.send(scrapperConfig.kafkaTopics().updates(), request);
        }
    }
}
