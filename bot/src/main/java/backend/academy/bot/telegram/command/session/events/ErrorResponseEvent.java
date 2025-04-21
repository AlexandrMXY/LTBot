package backend.academy.bot.telegram.command.session.events;

public record ErrorResponseEvent(Throwable error) implements ServerResponseEvent {
    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public String getUserMessage() {
        return error.getMessage();
    }
}
