package backend.academy.bot.telegram.commands;


import org.springframework.stereotype.Component;

@Component
public class StartCommand implements Command {
    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Command description here";
    }
}
