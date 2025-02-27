package backend.academy.bot.telegram.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import org.junit.jupiter.api.Test;

class AbstractSimpleCommandTest {
    @Test
    public void noArgsConstructor_whenCalled_throwIllegalStateExceptionOnUpdateState() {
        var instance = new AbstractSimpleCommandImpl();

        assertThrows(
                IllegalStateException.class,
                () -> instance.getSessionStateInitializer().initSessionState().updateState(null, null));
    }

    @Test
    public void updateState_whenCalled_callProcessor() {
        var processor = mock(SimpleCommandProcessor.class);
        var instance = new AbstractSimpleCommandImpl(processor);

        var state = instance.getSessionStateInitializer().initSessionState();

        var msg = new MessageDto(0, null);
        var context = new SessionContext();

        state.updateState(msg, context);

        verify(processor).processCommand(same(state), same(msg), same(context));
    }

    static class AbstractSimpleCommandImpl extends AbstractSimpleCommand {
        public AbstractSimpleCommandImpl() {}

        public AbstractSimpleCommandImpl(SimpleCommandProcessor processor) {
            super(processor);
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
