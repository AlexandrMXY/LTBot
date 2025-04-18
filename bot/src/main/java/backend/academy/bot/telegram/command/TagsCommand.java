package backend.academy.bot.telegram.command;

import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.ErrorResponseEvent;
import backend.academy.bot.telegram.command.session.events.SuccessResponseEvent;
import backend.academy.bot.telegram.formatters.LinksListFormatter;
import org.springframework.stereotype.Component;

@Component
public class TagsCommand extends AbstractSimpleRequestCommand {
    public TagsCommand(TelegramService telegramService, AsyncScrapperService scrapperService, SessionStateManager sessionStateManager, LinksListFormatter formatter) {
        super((message) -> {
            String[] args = message.message().split("\\s");

            if (args.length == 2) {
                if ("list".equals(args[1])) {
                    scrapperService.getTagsList(message.chatId()).subscribe(
                        (tags) -> sessionStateManager.onUpdate(message.chatId(),
                            new SuccessResponseEvent(tags.tags().isEmpty() ? "No muted tags" : "Muted tags:\n" + String.join(" ", tags.tags()))),
                        (t) -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                }
            } else if (args.length == 3) {
                if ("mute".equals(args[1])) {
                    scrapperService.deactivateTag(message.chatId(), args[2]).subscribe(
                        (success) -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent("Success")),
                        (t) -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                } else if ("unmute".equals(args[1])) {
                    scrapperService.reactivateTag(message.chatId(), args[2]).subscribe(
                        (success) -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent("Success")),
                        (t) -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                } else if ("list".equals(args[1])) {
                    scrapperService.getLinksWithTag(message.chatId(), args[2]).subscribe(
                        links -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent(formatter.format(links))),
                        t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                }
            } else if (args.length == 4) {
                if ("add".equals(args[1])) {
                    scrapperService.addTagToLink(message.chatId(), args[2], args[3]).subscribe(
                        (success) -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent("Success")),
                        (t) -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                } else if ("remove".equals(args[1])) {
                    scrapperService.removeTagFromLink(message.chatId(), args[2], args[3]).subscribe(
                        (success) -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent("Success")),
                        (t) -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                }
            }
            telegramService.sendMessage(message.chatId(), """
            Invalid command
            The following commands are available:
            /tags mute <tag> - mute all links with given tag (does nothing if the tag is already muted)
            /tags unmute <tag> - unmute tag (does nothing if the tag is not muted)
            /tags list - list of all muted tags
            /tags list <tag> - list of all links with given tag
            /tags add <link> <tag> - add tag to link
            /tags remove <link> <tag> - remove tag from link
            """);
            return false;
        }, telegramService);
    }

    @Override
    public String getName() {
        return "tags";
    }

    @Override
    public String getDescription() {
        return "manage tags";
    }
}
