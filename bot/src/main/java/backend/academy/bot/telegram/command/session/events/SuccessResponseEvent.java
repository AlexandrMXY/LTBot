package backend.academy.bot.telegram.command.session.events;

public record SuccessResponseEvent(String message) implements ServerResponseEvent {
    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public String getUserMessage() {
        return message;
    }
}
