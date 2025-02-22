package backend.academy.bot.service.telegram;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.service.telegram.CommandProcessorService;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.TelegramSessionState;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class UserSessionManagementService {
    private final Map<Long, TelegramSessionState> states = new HashMap<>();

    @Autowired
    private CommandProcessorService commandProcessorService;
    @Autowired
    private SessionContext context;
    @Autowired
    private TelegramService telegramService;

    public void processMessage(MessageDto message) {
        var state = states.get(message.chat());
        if (state == null || commandProcessorService.isCommand(message))  {
            var initializer = commandProcessorService.getSessionStateInitializer(message);
            state = initializer.initSessionState();
        }
        if (state == null) {
            states.remove(message.chat());
            return;
        }

        var updateResult = state.updateState(state, message, context);
        state = updateResult.newState();

        if (state == null) {
            states.remove(message.chat());
        }
        else {
            states.put(message.chat(), state);
        }

        telegramService.sendResponse(updateResult.response());
    }
}
