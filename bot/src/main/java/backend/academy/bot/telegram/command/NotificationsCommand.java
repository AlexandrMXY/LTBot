package backend.academy.bot.telegram.command;

import backend.academy.api.model.NotificationPolicy;
import backend.academy.bot.service.AsyncScrapperService;
import backend.academy.bot.service.telegram.TelegramService;
import backend.academy.bot.telegram.command.session.SessionStateManager;
import backend.academy.bot.telegram.command.session.events.ErrorResponseEvent;
import backend.academy.bot.telegram.command.session.events.SuccessResponseEvent;
import backend.academy.bot.utils.TimeUtil;
import org.springframework.stereotype.Component;

@Component
public class NotificationsCommand extends AbstractSimpleRequestCommand {
    public NotificationsCommand(TelegramService telegramService, AsyncScrapperService scrapperService, SessionStateManager sessionStateManager) {
        super((message) -> {
            String[] args = message.message().trim().split("\\s");
            if (args.length == 2) {
                if (args[1].equals("current")) {
                    scrapperService.getNotificationPolicy(message.chatId()).subscribe(
                        notificationPolicy -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent(getNotificationPolicyMessage(notificationPolicy))),
                        t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                } else if (args[1].equals("instant")) {
                    scrapperService.setNotificationPolicy(
                            message.chatId(),
                            new NotificationPolicy(NotificationPolicy.INSTANT, -1))
                        .subscribe(
                            success -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent("Success")),
                            t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                }
            } else if (args.length == 3) {
                if (args[1].equals("delayed")) {
                    int time = TimeUtil.getTimeOfDayFromString(args[2]);
                    if (time < 0) {
                        telegramService.sendMessage(message.chatId(), "Invalid time. Time must be in HH:mm format");
                        return false;
                    }
                    scrapperService.setNotificationPolicy(
                            message.chatId(),
                            new NotificationPolicy(NotificationPolicy.DELAYED, time))
                        .subscribe(
                            success -> sessionStateManager.onUpdate(message.chatId(), new SuccessResponseEvent("Success")),
                            t -> sessionStateManager.onUpdate(message.chatId(), new ErrorResponseEvent(t)));
                    return true;
                }
            }
            telegramService.sendMessage(message.chatId(), """
                Invalid command
                The following commands are available:
                /notifications current - display current notifications policy
                /notifications instant - send notifications immediately
                /notifications delayed <time> - send notifications at a specified time
                """);
            return false;
        }, telegramService);
    }

    private static String getNotificationPolicyMessage(NotificationPolicy policy) {
        if ("INSTANT".equals(policy.strategy()))
            return "Notifications are sent immediately";
        return "Notifications are sent at " + TimeUtil.getTimeFromMinuteOfDayOrErrorString(policy.time());
    }

    @Override
    public String getName() {
        return "notifications";
    }

    @Override
    public String getDescription() {
        return "manage notifications time";
    }
}
