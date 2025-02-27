package backend.academy.bot.service.telegram;

import backend.academy.bot.dto.MessageDto;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@Profile("prod")
public class TelegramEventHandlerService {
    @Autowired
    private TelegramService telegramService;

    @Autowired
    private UserSessionManagementService userSessionManagementService;

    @PostConstruct
    private void initListener() {
        telegramService.getBot().setUpdatesListener((updates) -> {
            updates.forEach(update -> {
                Message message = update.message();
                if (message != null) {
                    userSessionManagementService.processMessage(new MessageDto(message));
                }
            });

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
