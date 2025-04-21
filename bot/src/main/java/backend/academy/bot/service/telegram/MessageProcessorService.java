package backend.academy.bot.service.telegram;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.command.Command;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageProcessorService {
    public static final String COMMAND_PREFIX = "/";

    private final Map<String, Command> commands;
    private final TelegramBot bot;
    private final TelegramService telegramService;
    private final SessionStateManager sessionStateManager;

    @Autowired
    public MessageProcessorService(
            List<Command> commands,
            TelegramBot bot,
            TelegramService telegramService,
            SessionStateManager sessionStateManager) {
        this.bot = bot;
        this.commands = commands.stream().collect(Collectors.toMap(Command::getName, command -> command));
        this.telegramService = telegramService;
        this.sessionStateManager = sessionStateManager;
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

    public void processMessage(MessageDto message) {
        if (isCommand(message)) {
            Command command = getCommand(message);
            if (command == null) {
                telegramService.sendMessage(message.chat(), "Unknown command");
                return;
            }
            sessionStateManager.onCommand(message.chat(), command, new MessageEvent(message));
            return;
        }
        sessionStateManager.onUpdate(message.chat(), new MessageEvent(message));
    }
}
