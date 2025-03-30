package backend.academy.bot.telegram.commands;

import backend.academy.api.exceptions.ApiErrorResponseException;
import backend.academy.bot.telegram.session.TelegramResponse;
import org.springframework.stereotype.Component;

@Component
public class TagsCommand extends AbstractSimpleCommand {
    public TagsCommand() {
        super((state, message, context) -> {
            String[] args = message.message().split(" ");
            if (!(args.length == 3 && (args[1].equals("mute") || args[1].equals("unmute")))
                    && !(args.length == 2 && args[1].equals("list"))) {
                return new TelegramResponse(
                        message.chat(), "Invalid command. Use /tags <mute/unmute> <tag> or /tags list");
            }

            try {
                if (args[1].equals("mute")) context.scrapperService().deactivateTag(message.chat(), args[2]);
                else if (args[1].equals("unmute")) context.scrapperService().reactivateTag(message.chat(), args[2]);
                else {
                    var tags = context.scrapperService().getTagsList(message.chat());
                    return new TelegramResponse(message.chat(), "Muted tags:\n" + String.join(" ", tags.tags()));
                }
                return new TelegramResponse(message.chat(), "Success");
            } catch (ApiErrorResponseException e) {
                return new TelegramResponse(message.chat(), "An error occurred");
            }
        });
    }

    @Override
    public String getName() {
        return "tags";
    }

    @Override
    public String getDescription() {
        return "manage muted tags";
    }
}
