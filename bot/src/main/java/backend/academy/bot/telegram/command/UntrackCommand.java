package backend.academy.bot.telegram.command;

import backend.academy.api.model.requests.RemoveLinkRequest;
import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.ErrorResponseEvent;
import backend.academy.bot.telegram.command.session.events.LinkResponseEvent;
import org.springframework.stereotype.Component;

@Component
public class UntrackCommand extends AbstractSimpleRequestCommand {
    public UntrackCommand(
            TelegramService telegramService,
            AsyncScrapperService scrapperService,
            SessionStateManager sessionStateManager) {
        super(
                (message) -> {
                    String[] msg = message.message().trim().split(" ");
                    if (msg.length != 2) {
                        telegramService.sendMessage(message.chatId(), "Use /untrack <url>");
                        return false;
                    }

                    String url = msg[1];
                    scrapperService
                            .removeLink(message.chatId(), new RemoveLinkRequest(url))
                            .subscribe(
                                    response -> sessionStateManager.onUpdate(
                                            message.chatId(), new LinkResponseEvent(response)),
                                    t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                },
                telegramService);
    }

    @Override
    public String getName() {
        return "untrack";
    }

    @Override
    public String getDescription() {
        return "stop tracking link";
    }
}
