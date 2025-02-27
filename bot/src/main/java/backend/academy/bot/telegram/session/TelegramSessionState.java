package backend.academy.bot.telegram.session;

import backend.academy.bot.dto.MessageDto;

public class TelegramSessionState {
    public SessionUpdateResult updateState(MessageDto message, SessionContext context) {
        return null;
    }

    public record SessionUpdateResult(
        TelegramSessionState newState,
        TelegramResponse response
    ) {
    }
}
