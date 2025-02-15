package backend.academy.bot.service.telegram;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.commands.Command;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CommandProcessorService {
    public static final String COMMAND_PREFIX = "/";

    private final Map<String, Command> commands;
    private final TelegramBot bot;

    @Autowired
    @Qualifier("unknownCommandSessionStateInitializer")
    private SessionStateInitializer unknownCommandSessionStateInitializer;


    @Autowired
    public CommandProcessorService(List<Command> commands, TelegramBot bot) {
        this.bot = bot;
        this.commands = commands.stream().collect(
            Collectors.toMap(Command::getName, command -> command));
    }

    @PostConstruct
    private void registerCommands() {
        var res = bot.execute(
            new SetMyCommands(
                commands.values().stream()
                    .filter(command -> !command.isHidden())
                    .map(command -> new BotCommand(command.getName(), command.getDescription()))
                    .toArray(BotCommand[]::new)));
        if (!res.isOk()) {
            log.error("Error registering commands: {}", res);
        }
    }


    public boolean isCommand(MessageDto message) {
        return message.message().startsWith(COMMAND_PREFIX);
    }

    public SessionStateInitializer getSessionStateInitializer(MessageDto messageDto) {
        if (isCommand(messageDto)) {
            int delimIndex = messageDto.message().indexOf(' ');
            var commandName = messageDto.message().substring(
                COMMAND_PREFIX.length(),
                delimIndex == -1 ? messageDto.message().length() : delimIndex);
            if (commands.containsKey(commandName)) {
                return commands.get(commandName).getSessionStateInitializer();
            }
            return unknownCommandSessionStateInitializer;
        }
        // TODO
        return () -> null;
    }
}
