package backend.academy.api.model;

public record LinkUpdate(long chatId, long time, String url, String content, String author) {}
