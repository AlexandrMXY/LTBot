package backend.academy.bot.telegram.session;

import backend.academy.bot.dto.MessageDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("unknownCommandSessionStateInitializer")
public class UnknownCommandSessionStateInitializer implements SessionStateInitializer {
    @Override
    public TelegramSessionState initSessionState() {
        return new TelegramSessionState() {
            @Override
            public TelegramSessionState.SessionUpdateResult updateState(
                MessageDto message,
                SessionContext context) {
                return new SessionUpdateResult(
                    null,
                    new TelegramResponse(message.chat(), "Unknown command"));
            }
        };
    }
}
