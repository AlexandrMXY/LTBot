package backend.academy.bot.telegram.command.session.events;

import backend.academy.bot.telegram.command.session.SessionEvent;

public interface ServerResponseEvent extends SessionEvent {
    boolean isError();

    String getUserMessage();
}
