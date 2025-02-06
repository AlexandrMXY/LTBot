package backend.academy.bot.telegram.commands;

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

    public HelpCommand() {
        super(((state, message, context) -> {
            throw new IllegalStateException("SimpleCommandProcessor not initialized");
        }));
    }

    @PostConstruct
    private void init() {
        StringBuilder mb = new StringBuilder();
        for (Command command : commands) {
            mb
                .append("/")
                .append(command.getName())
                .append(": ")
                .append(command.getDescription())
                .append(System.lineSeparator());
        }

        setProcessor((state, message, context) ->
            context.telegramService().sendMessage(message.chat(), mb.toString()));
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Command description here";
    }
}
