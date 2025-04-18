package backend.academy.bot.telegram.command;

import backend.academy.api.model.requests.AddLinkRequest;
import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.events.ErrorResponseEvent;
import backend.academy.bot.telegram.command.session.events.LinkResponseEvent;
import backend.academy.bot.telegram.command.session.events.MessageEvent;
import backend.academy.bot.telegram.command.session.SessionEvent;
import backend.academy.bot.telegram.command.session.SessionState;
import backend.academy.bot.telegram.command.session.SessionStateHandler;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.ServerResponseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class TrackCommand implements Command {
    private static final String URL_KEY = "url";
    private static final String TAGS_KEY = "tags";

    private final SessionStateHandler initHandler;
    private final SessionStateHandler urlInputHandler;
    private final SessionStateHandler tagsHandler;
    private final SessionStateHandler filtersHandler;
    private final SessionStateHandler serverResponseHandler;

    public TrackCommand(TelegramService telegramService, AsyncScrapperService scrapperService, SessionStateManager sessionStateManager) {
        serverResponseHandler = (state, event0) -> {
            if (!(event0 instanceof ServerResponseEvent event)) {
                return false;
            }
            telegramService.sendMessage(state.chatId(), event.getUserMessage());
            return false;
        };

        filtersHandler = (state, event0) -> {
            if (!(event0 instanceof MessageEvent event)) {
                return true;
            }

            List<String> filters;
            if ("none".equalsIgnoreCase(event.message().trim())) {
                filters = List.of();
            } else {
                filters = Arrays.stream(event.message().trim().split("\\s")).toList();
            }
            List<String> tags = state.getValue(TAGS_KEY);
            String url = state.getValue(URL_KEY);

            scrapperService.trackRequest(state.chatId(), new AddLinkRequest(url, tags, filters))
                .subscribe(
                    (response -> sessionStateManager.onUpdate(state.chatId(), new LinkResponseEvent(response))),
                    (t -> sessionStateManager.onUpdate(state.chatId(), new ErrorResponseEvent(t))));
            state.stateHandler(serverResponseHandler);
            return true;
        };

        tagsHandler = ((state, event0) -> {
            if (!(event0 instanceof MessageEvent event)) {
                return true;
            }

            state.setValue(TAGS_KEY, Arrays.stream(event.message().trim().split("\\s")).toList());
            state.stateHandler(filtersHandler);
            telegramService.sendMessage(state.chatId(), "Enter filters (or type \"None\" to continue without filters):");
            return true;
        });

        urlInputHandler = ((state, event0) -> {
            if (!(event0 instanceof MessageEvent event)) {
                return true;
            }

            if (checkUrl(event.message())) {
                telegramService.sendMessage(state.chatId(), "Enter tags:");
                state.setValue(URL_KEY, event.message());
                state.stateHandler(tagsHandler);
                return true;
            }
            telegramService.sendMessage(state.chatId(), "Invalid url");
            telegramService.sendMessage(state.chatId(), "Enter url:");
            return true;
        });

        initHandler = ((state, event0) -> {
            if (!(event0 instanceof MessageEvent event)) {
                return true;
            }

            if (!"/track".equals(event.message().trim())) {
                telegramService.sendMessage(event.chatId(), "Invalid command");
                return false;
            }

            telegramService.sendMessage(event.chatId(), "Enter url:");
            state.stateHandler(urlInputHandler);
            return true;
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean checkUrl(String url) {
        try {
            new URI(url).toURL();
            return true;
        } catch (URISyntaxException | IllegalArgumentException | MalformedURLException e) {
            return false;
        }
    }

    @Override
    public SessionState initSession(long chatId) {
        return new SessionState(initHandler, chatId);
    }

    @Override
    public String getDescription() {
        return "start tracking link";
    }

    @Override
    public String getName() {
        return "track";
    }
}
