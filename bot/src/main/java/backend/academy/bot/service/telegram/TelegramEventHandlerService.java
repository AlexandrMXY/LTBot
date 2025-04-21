package backend.academy.bot.service.telegram;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.command.Command;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
@Slf4j
public class TelegramEventHandlerService {
    @Autowired
    private TelegramService telegramService;
    @Autowired
    private MessageProcessorService messageProcessorService;

    @PostConstruct
    private void initListener() {
        telegramService.getBot().setUpdatesListener((updates) -> {
            updates.forEach(update -> {
                try {
                    Message message = update.message();
                    if (message != null) {
                        messageProcessorService.processMessage(new MessageDto(message));
                    }
                } catch (Throwable t) {
                    log.atWarn()
                            .setMessage("An error occurred during message processing")
                            .setCause(t)
                            .log();
                }
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }


}
