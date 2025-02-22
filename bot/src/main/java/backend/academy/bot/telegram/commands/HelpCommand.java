package backend.academy.bot.telegram.commands;

import backend.academy.bot.telegram.session.TelegramResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class HelpCommand extends AbstractSimpleCommand {
    @Autowired
    @Lazy
    private List<Command> commands;

    @PostConstruct
    private void init() {
        StringBuilder mb = new StringBuilder();
        mb.append("/").append(getName()).append(": ").append(getDescription()).append(System.lineSeparator());
        for (Command command : commands) {
            if (command.isHidden()) {
                continue;
            }
            mb
                .append("/")
                .append(command.getName())
                .append(": ")
                .append(command.getDescription())
                .append(System.lineSeparator());
        }

        setProcessor((state, message, context) ->
            new TelegramResponse(message.chat(), mb.toString()));
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "display all commands";
    }
}
