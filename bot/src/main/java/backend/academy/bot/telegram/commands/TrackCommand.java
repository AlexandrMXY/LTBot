package backend.academy.bot.telegram.commands;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.session.TelegramSessionState;
import backend.academy.bot.utils.RegExUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class TrackCommand implements Command {
    @Override
    public String getName() {
        return "track";
    }

    @Override
    public String getDescription() {
        return "Command description here";
    }

    @Override
    public SessionStateInitializer getSessionStateInitializer() {
        return new TrackSessionStateInitializer();
    }

    public static class TrackSessionStateInitializer implements SessionStateInitializer {
        @Override
        public TelegramSessionState initSessionState() {
            return new TrackSessionState();
        }
    }

    public static class TrackSessionState extends TelegramSessionState {
        private static final Logger LOGGER = LogManager.getLogger();

        private static enum Stage {
            INIT,
            URL_INPUT,
            TAGS_INPUT
        }

        private Stage stage = Stage.INIT;

        private String url;
        private List<String> tags = new ArrayList<>();

        @Override
        public TelegramSessionState updateState(TelegramSessionState state, MessageDto message, SessionContext context) {
            switch (stage) {
                case INIT -> {
                    context.telegramService().sendMessage(message.chat(), "Enter URL:");
                    stage = Stage.URL_INPUT;
                }
                case URL_INPUT -> {
                    if (RegExUtil.isStringSatisfyRegex(message.message(), context.urlRegEx())) {
                        context.telegramService().sendMessage(message.chat(), "Enter tags:");
                        stage = Stage.TAGS_INPUT;
                    } else {
                        context.telegramService().sendMessage(message.chat(), "Invalid URL");
                    }
                }
                case TAGS_INPUT -> {
                    tags.addAll(Arrays.stream(message.message().trim().split("\\s")).toList());
                    // TODO
                    LOGGER.info(this);
                    return null;
                }

            }
            LOGGER.info(this);
            return this;
        }
    }
}
