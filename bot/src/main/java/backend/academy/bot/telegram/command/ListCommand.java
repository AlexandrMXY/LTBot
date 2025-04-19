package backend.academy.bot.telegram.command;

import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.ErrorResponseEvent;
import backend.academy.bot.telegram.command.session.events.SuccessResponseEvent;
import backend.academy.bot.telegram.formatters.LinksListFormatter;
import org.springframework.stereotype.Component;

@Component
public class ListCommand extends AbstractSimpleRequestCommand {
    public ListCommand(
            TelegramService telegramService,
            AsyncScrapperService scrapperService,
            SessionStateManager sessionStateManager,
            LinksListFormatter formatter) {
        super((message) -> {
            if (!"/list".equals(message.message().trim())) {
                telegramService.sendMessage(message.chatId(), "Invalid command");
                return false;
            }
            scrapperService.getTrackedLinks(message.chatId()).subscribe(
                links -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent(formatter.format(links))),
                t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
            return true;
        }, telegramService);
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "displays all tracked links";
    }
}
