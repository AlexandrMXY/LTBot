package backend.academy.bot.telegram.commands;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.session.TelegramSessionState;

public abstract class AbstractSimpleCommand implements Command {
    private SessionStateInitializer initializer;

    public AbstractSimpleCommand() {
        this((state, message, context) -> {
            throw new IllegalStateException("Command processor not initialized");
        });
    }

    public AbstractSimpleCommand(SimpleCommandProcessor processor) {
        setProcessor(processor);
    }

    protected void setProcessor(SimpleCommandProcessor processor) {
        initializer = new SimpleCommandSessionStateInitializer(processor);
    }

    @Override
    public SessionStateInitializer getSessionStateInitializer() {
        return initializer;
    }

    public static class SimpleCommandSessionStateInitializer implements SessionStateInitializer {
        private final SimpleCommandProcessor processor;

        public SimpleCommandSessionStateInitializer(SimpleCommandProcessor processor) {
            this.processor = processor;
        }

        @Override
        public TelegramSessionState initSessionState() {
            return new SimpleCommandSessionState(processor);
        }
    }

    public static class SimpleCommandSessionState extends TelegramSessionState {
        private final SimpleCommandProcessor processor;

        public SimpleCommandSessionState(SimpleCommandProcessor processor) {
            this.processor = processor;
        }

        @Override
        public TelegramSessionState.SessionUpdateResult updateState(MessageDto message, SessionContext context) {
            return new SessionUpdateResult(null, processor.processCommand(this, message, context));
        }
    }
}
