package backend.academy.bot.telegram.session;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.dto.MessageDto;
import org.junit.jupiter.api.Test;

class UnknownCommandSessionStateInitializerTest {
    @Test
    public void initSessionState_whenCalled_returnNotNull() {
        assertNotNull(new UnknownCommandSessionStateInitializer().initSessionState());
    }

    @Test
    public void updateState_whenCalled_returnTelegramResponseToCorrectUserAndNullNewState() {
        var state = new UnknownCommandSessionStateInitializer().initSessionState();
        var msg = new MessageDto(1111, null);

        var res = state.updateState(msg, null);

        assertNull(res.newState());
        assertEquals(msg.chat(), res.response().userId());
        assertNotEquals(0, res.response().messages().size());
    }
}
