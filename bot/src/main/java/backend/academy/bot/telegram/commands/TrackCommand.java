package backend.academy.bot.telegram.commands;

import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.session.TelegramSessionState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TrackCommand implements Command {
    @Override
    public String getName() {
        return "track";
    }

    @Override
    public String getDescription() {
        return "start tracking link";
    }

    @Override
    public SessionStateInitializer getSessionStateInitializer() {
        return new TrackSessionStateInitializer();
    }

    public static class TrackSessionStateInitializer implements SessionStateInitializer {
        @Override
        public TelegramSessionState initSessionState() {
            return new TrackSessionState();
        }
    }
}
