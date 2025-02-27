package backend.academy.bot.dto;

import com.pengrad.telegrambot.model.Message;

public record MessageDto(long chat, String message) {
    public MessageDto(Message message) {
        this(message.chat().id(), message.text());
    }
}
