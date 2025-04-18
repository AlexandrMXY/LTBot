package backend.academy.bot.telegram.command;

import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionState;
import backend.academy.bot.telegram.command.session.SessionStateHandler;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import backend.academy.bot.telegram.command.session.events.ServerResponseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class AbstractSimpleRequestCommand implements Command {
    private final SessionStateHandler initHandler;
    private final SessionStateHandler responseHandler;

    public AbstractSimpleRequestCommand(Predicate<MessageEvent> commandHandler, TelegramService telegramService) {
        responseHandler = ((state, event0) -> {
            if (!(event0 instanceof ServerResponseEvent event)) {
                return false;
            }
            telegramService.sendMessage(state.chatId(), event.getUserMessage());
            return false;
        });

        initHandler = ((state, event) -> {
            if (event instanceof MessageEvent messageEvent) {
                boolean stillActive = commandHandler.test(messageEvent);
                if (!stillActive) {
                    return false;
                }
                state.stateHandler(responseHandler);
            }
            return true;
        });
    }

    @Override
    public SessionState initSession(long chatId) {
        return new SessionState(initHandler, chatId);
    }
}
