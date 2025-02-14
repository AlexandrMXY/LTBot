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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommandProcessorService {
    public static final String COMMAND_PREFIX = "/";

    private final Map<String, Command> commands;
    private final TelegramService telegramService;
    private final TelegramBot bot;

    @Autowired
    @Qualifier("unknownCommandSessionStateInitializer")
    private SessionStateInitializer unknownCommandSessionStateInitializer;


    @Autowired
    public CommandProcessorService(List<Command> commands, TelegramService telegramService, TelegramBot bot) {
        this.telegramService = telegramService;
        this.bot = bot;
        this.commands = commands.stream().collect(
            Collectors.toMap(Command::getName, command -> command));
    }

    @PostConstruct
    private void registerCommands() {
        bot.execute(
            new SetMyCommands(
                commands.values().stream()
                    .map(command -> new BotCommand(command.getName(), command.getDescription()))
                    .toList()
                    .toArray(new BotCommand[0])));
    }


    public boolean isCommand(MessageDto message) {
        return message.message().startsWith(COMMAND_PREFIX);
    }

    public SessionStateInitializer getSessionStateInitializer(MessageDto messageDto) {
        if (isCommand(messageDto)) {
            var commandName = messageDto.message().substring(COMMAND_PREFIX.length());
            if (commands.containsKey(commandName)) {
                return commands.get(commandName).getSessionStateInitializer();
            }
            return unknownCommandSessionStateInitializer;
        }
        // TODO
        return () -> null;
    }
}
