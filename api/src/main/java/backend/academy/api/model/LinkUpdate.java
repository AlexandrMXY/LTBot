package backend.academy.api.model;

import java.util.List;

public record LinkUpdate(long chatId, long time, String url, String content, String author) {}
