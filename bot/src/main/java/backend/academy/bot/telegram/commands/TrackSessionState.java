package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.api.model.responses.ApiErrorResponse;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.session.SessionContext;
import backend.academy.bot.telegram.session.TelegramResponse;
import backend.academy.bot.telegram.session.TelegramSessionState;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrackSessionState extends TelegramSessionState {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private boolean checkUrl(String url) {
        try {
            new URI(url).toURL();
            return true;
        } catch (URISyntaxException | IllegalArgumentException | MalformedURLException e) {
            return false;
        }
    }

    @Override
    public SessionUpdateResult updateState(MessageDto message, SessionContext context) {
        TelegramResponse response = null;
        switch (stage) {
            case INIT -> {
                response = new TelegramResponse(message.chat(), "Enter URL:");
                stage = Stage.URL_INPUT;
            }
            case URL_INPUT -> {
                url = message.message();
                if (checkUrl(url)) {
                    response = new TelegramResponse(message.chat(), "Enter tags:");
                    stage = Stage.TAGS_INPUT;
                } else {
                    response = new TelegramResponse(message.chat(), "Invalid URL");
                }
            }
            case TAGS_INPUT -> {
                tags.addAll(Arrays.stream(message.message().trim().split("\\s")).toList());
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
            LinkResponse response = context.scrapperService().addLink(chatId, new AddLinkRequest(url, tags, filters));
            return "Tracking " + response.url();
        } catch (ApiErrorResponseException exception) {
            ApiErrorResponse response = exception.details();
            return response.exceptionMessage();
        } catch (Exception t) {
            log.atError().setMessage("Exception occurred").setCause(t).log();
        }
        return null;
    }
}
