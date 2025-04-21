package backend.academy.bot.telegram.command;

import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionState;
import backend.academy.bot.telegram.command.session.SessionStateHandler;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {
    @Autowired
    @Lazy
    private List<Command> commands;

    @Autowired
    private TelegramService telegramService;

    private SessionStateHandler handler;

    @PostConstruct
    private void init() {
        StringBuilder mb = new StringBuilder();
        mb.append("/").append(getName()).append(": ").append(getDescription()).append(System.lineSeparator());
        for (Command command : commands) {
            if (command.isHidden()) {
                continue;
            }
            mb.append("/")
                    .append(command.getName())
                    .append(": ")
                    .append(command.getDescription())
                    .append(System.lineSeparator());
        }

        handler = (state, event) -> {
            if (event instanceof MessageEvent messageEvent
                    && !"/help".equals(messageEvent.message().trim())) {
                telegramService.sendMessage(state.chatId(), "Invalid command");
                return false;
            }
            telegramService.sendMessage(state.chatId(), mb.toString());
            return false;
        };
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "display all commands";
    }

    @Override
    public SessionState initSession(long chatId) {
        return new SessionState(handler, chatId);
    }
}
