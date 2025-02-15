package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.session.TelegramSessionState;
import backend.academy.bot.utils.RegExUtil;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Log4j2
public class TrackCommand implements Command {
    @Override
    public String getName() {
        return "track";
    }

    @Override
    public String getDescription() {
        return "start tracking link";
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

        private enum Stage {
            INIT,
            URL_INPUT,
            TAGS_INPUT,
            FILTERS_INPUT
        }

        private Stage stage = Stage.INIT;

        private String url;
        private final List<String> tags = new ArrayList<>();
        private final List<String> filters = new ArrayList<>();

        @Override
        public TelegramSessionState updateState(TelegramSessionState state, MessageDto message, SessionContext context) {
            switch (stage) {
                case INIT -> {
                    context.telegramService().sendMessage(message.chat(), "Enter URL:");
                    stage = Stage.URL_INPUT;
                }
                case URL_INPUT -> {
                    url = message.message();
                    if (RegExUtil.isStringSatisfyRegex(message.message(), context.urlRegEx())) {
                        context.telegramService().sendMessage(message.chat(), "Enter tags:");
                        stage = Stage.TAGS_INPUT;
                    } else {
                        context.telegramService().sendMessage(message.chat(), "Invalid URL");
                    }
                }
                case TAGS_INPUT -> {
                    tags.addAll(Arrays.stream(message.message().trim().split("\\s")).toList());
                    stage = Stage.FILTERS_INPUT;
                    context.telegramService().sendMessage(message.chat(), "Enter filters:");
                }
                case FILTERS_INPUT -> {
                    filters.addAll(Arrays.stream(message.message().trim().split("\\s")).toList());
                    context.telegramService().sendMessage(message.chat(), "Success");

                    registerLink(context, message.chat());

                    return null;
                }
            }
            return this;
        }

        private void registerLink(SessionContext context, long chatId) {
            try {
                LinkResponse response = context.scrapperService().addLink(
                    chatId, new AddLinkRequest(url, tags, filters));
                context.telegramService().sendMessage(chatId, "Tracking " + response.url());
            } catch (ErrorResponseException exception) {
                ApiErrorResponse response = exception.details();

                if (response == null) {
                    log.warn("Invalid response: {}", "", exception);
                } else {
                    context.telegramService().sendMessage(chatId, response.exceptionMessage());
                }
            } catch (Exception t) {
                LOGGER.error("", t);
            }
        }
    }
}
