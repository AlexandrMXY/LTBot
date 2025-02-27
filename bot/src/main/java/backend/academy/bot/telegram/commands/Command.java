package backend.academy.bot.telegram.commands;

import backend.academy.bot.telegram.session.SessionStateInitializer;

public interface Command {
    String getName();

    String getDescription();

    default boolean isHidden() {
        return false;
    }

    default SessionStateInitializer getSessionStateInitializer() {
        return () -> null;
    }
}
