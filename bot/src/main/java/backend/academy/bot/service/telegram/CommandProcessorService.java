package backend.academy.bot.service.telegram;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.command.Command;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.session.TelegramResponse;
import backend.academy.bot.telegram.session.TelegramSessionState;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommandProcessorService {
    public static final String COMMAND_PREFIX = "/";

    private final Map<String, Command> commands;
    private final TelegramBot bot;


    @Autowired
    public CommandProcessorService(List<Command> commands, TelegramBot bot) {
        this.bot = bot;
        this.commands = commands.stream().collect(Collectors.toMap(Command::getName, command -> command));
    }

    @PostConstruct
    private void registerCommands() {
        var res = bot.execute(new SetMyCommands(commands.values().stream()
                .filter(command -> !command.isHidden())
                .map(command -> new BotCommand(command.getName(), command.getDescription()))
                .toArray(BotCommand[]::new)));
        if (!res.isOk()) {
            log.atError()
                    .setMessage("Unable to set bot commands: received error response")
                    .addKeyValue("code", res.errorCode())
                    .addKeyValue("description", res.description())
                    .addKeyValue("parameters", res.parameters())
                    .log();
        }
    }

    public boolean isCommand(MessageDto message) {
        return message.message().startsWith(COMMAND_PREFIX);
    }

    public Command getCommand(MessageDto messageDto) {
        if (isCommand(messageDto)) {
            int delimIndex = messageDto.message().indexOf(' ');
            var commandName = messageDto
                    .message()
                    .substring(
                            COMMAND_PREFIX.length(),
                            delimIndex == -1 ? messageDto.message().length() : delimIndex);
            return commands.get(commandName);
        }
        return null;
    }
}
