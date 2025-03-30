package backend.academy.api.model;

import java.util.List;

public record TagsListResponse(long chatId, List<String> tags) {}
