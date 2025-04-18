package backend.academy.bot.telegram.command.session.events;

import backend.academy.api.model.responses.LinkResponse;
import backend.academy.bot.telegram.command.session.SessionEvent;

public record LinkResponseEvent(
    LinkResponse response
) implements ServerResponseEvent {
    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public String getUserMessage() {
        return "Success";
    }
}
