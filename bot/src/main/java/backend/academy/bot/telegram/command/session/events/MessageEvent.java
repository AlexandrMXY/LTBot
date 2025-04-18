package backend.academy.bot.telegram.command.session.events;

import backend.academy.bot.dto.MessageDto;
import backend.academy.bot.telegram.command.session.SessionEvent;

public record MessageEvent(
    long chatId,
    String message
) implements SessionEvent {
    public MessageEvent(MessageDto dto) {
        this(dto.chat(), dto.message());
    }
}
