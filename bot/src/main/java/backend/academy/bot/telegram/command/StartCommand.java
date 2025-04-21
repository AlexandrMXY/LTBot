package backend.academy.bot.telegram.command;

import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.ErrorResponseEvent;
import backend.academy.bot.telegram.command.session.events.SuccessResponseEvent;
import org.springframework.stereotype.Component;

@Component
public class StartCommand extends AbstractSimpleRequestCommand {
    public StartCommand(
            TelegramService telegramService,
            AsyncScrapperService scrapperService,
            SessionStateManager sessionStateManager) {
        super(
                (message) -> {
                    if (!"/start".equals(message.message().trim())) {
                        telegramService.sendMessage(message.chatId(), "Invalid command");
                        return false;
                    }
                    scrapperService
                            .registerChat(message.chatId())
                            .subscribe(
                                    success -> sessionStateManager.onUpdate(
                                            message.chatId(), new SuccessResponseEvent("Welcome")),
                                    t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                },
                telegramService);
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "start";
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
