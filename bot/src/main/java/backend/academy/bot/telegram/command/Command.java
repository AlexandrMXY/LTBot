package backend.academy.bot.telegram.command;

import backend.academy.bot.telegram.command.session.SessionEvent;
import backend.academy.bot.telegram.command.session.SessionState;

public interface Command {
    SessionState initSession(long chatId);
    String getName();
    String getDescription();
    default boolean isHidden() {
        return false;
    }
}
