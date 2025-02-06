package backend.academy.bot.telegram.commands;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.TelegramSessionState;

@FunctionalInterface
public interface SimpleCommandProcessor {
    void processCommand(TelegramSessionState state, MessageDto message, SessionContext context);
}
