package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.api.model.responses.LinkResponse;
import backend.academy.api.model.responses.ListLinksResponse;
import backend.academy.api.model.responses.TagsListResponse;
import backend.academy.bot.telegram.session.TelegramResponse;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TagsCommand extends AbstractSimpleCommand {
    public TagsCommand() {
        super((state, message, context) -> {
            String[] args = message.message().split(" ");

            try {
                if (args.length == 2) {
                    if ("list".equals(args[1])) {
                        TagsListResponse tags = context.scrapperService().getTagsList(message.chat());
                        if (tags.tags().isEmpty()) return new TelegramResponse(message.chat(), "No muted tags");
                        return new TelegramResponse(message.chat(), "Muted tags:\n" + String.join(" ", tags.tags()));
                    }
                } else if (args.length == 3) {
                    if ("mute".equals(args[1])) {
                        context.scrapperService().deactivateTag(message.chat(), args[2]);
                        return new TelegramResponse(message.chat(), "Success");
                    } else if ("unmute".equals(args[1])) {
                        context.scrapperService().reactivateTag(message.chat(), args[2]);
                        return new TelegramResponse(message.chat(), "Success");
                    } else if ("list".equals(args[1])) {
                        ListLinksResponse links = context.scrapperService().getLinksWithTag(message.chat(), args[2]);
                        return new TelegramResponse(message.chat(), createLinksListMessage(links.links()));
                    }
                } else if (args.length == 4) {
                    if ("add".equals(args[1])) {
                        context.scrapperService().addTagToLink(message.chat(), args[2], args[3]);
                        return new TelegramResponse(message.chat(), "Success");
                    } else if ("remove".equals(args[1])) {
                        context.scrapperService().removeTagFromLink(message.chat(), args[2], args[3]);
                        return new TelegramResponse(message.chat(), "Success");
                    }
                }

                return new TelegramResponse(
                        message.chat(),
                        """
                    Invalid command
                    The following commands are available:
                    /tags mute <tag> - mute all links with given tag (does nothing if the tag is already muted)
                    /tags unmute <tag> - unmute tag (does nothing if the tag is not muted)
                    /tags list - list of all muted tags
                    /tags list <tag> - list of all links with given tag
                    /tags add <link> <tag> - add tag to link
                    /tags remove <link> <tag> - remove tag from link
                    """);
            } catch (ApiErrorResponseException e) {
                if (e.details() == null || e.details().description().isEmpty())
                    return new TelegramResponse(message.chat(), "An error occurred");
                return new TelegramResponse(message.chat(), e.details().description());
            }
        });
    }

    private static String createLinksListMessage(List<LinkResponse> links) {
        if (links.isEmpty()) return "No links found";
        StringBuilder builder = new StringBuilder();
        for (LinkResponse link : links) {
            builder.append(link.url());
            for (String tag : link.tags()) {
                builder.append(" ").append(tag);
            }
            builder.append("\n");
        }
        return builder.toString();
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
