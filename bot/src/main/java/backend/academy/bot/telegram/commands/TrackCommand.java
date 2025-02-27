package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.model.LinkResponse;
import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.SessionStateInitializer;
import backend.academy.bot.telegram.session.TelegramResponse;
import backend.academy.bot.telegram.session.TelegramSessionState;
import backend.academy.bot.utils.RegExUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

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
        public TelegramSessionState.SessionUpdateResult updateState(MessageDto message, SessionContext context) {
            TelegramResponse response = null;
            switch (stage) {
                case INIT -> {
                    response = new TelegramResponse(message.chat(), "Enter URL:");
                    stage = Stage.URL_INPUT;
                }
                case URL_INPUT -> {
                    url = message.message();
                    if (RegExUtil.isStringSatisfyRegex(message.message(), context.urlRegEx())) {
                        response = new TelegramResponse(message.chat(), "Enter tags:");
                        stage = Stage.TAGS_INPUT;
                    } else {
                        response = new TelegramResponse(message.chat(), "Invalid URL");
                    }
                }
                case TAGS_INPUT -> {
                    tags.addAll(
                            Arrays.stream(message.message().trim().split("\\s")).toList());
                    stage = Stage.FILTERS_INPUT;

                    response = new TelegramResponse(message.chat(), "Enter filters:");
                }
                case FILTERS_INPUT -> {
                    filters.addAll(
                            Arrays.stream(message.message().trim().split("\\s")).toList());

                    response = new TelegramResponse(message.chat(), registerLink(context, message.chat()));

                    return new SessionUpdateResult(null, response);
                }
            }
            return new SessionUpdateResult(this, response);
        }

        private String registerLink(SessionContext context, long chatId) {
            try {
                LinkResponse response =
                        context.scrapperService().addLink(chatId, new AddLinkRequest(url, tags, filters));
                return "Tracking " + response.url();
            } catch (ApiErrorResponseException exception) {
                ApiErrorResponse response = exception.details();

                if (response == null) {
                    log.warn("Invalid response: {}", "", exception);
                } else {
                    return response.exceptionMessage();
                }
            } catch (Exception t) {
                LOGGER.error("", t);
            }
            return null;
        }
    }
}
