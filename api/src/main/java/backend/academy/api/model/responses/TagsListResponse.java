package backend.academy.api.model.responses;

import java.util.List;

public record TagsListResponse(long chatId, List<String> tags) {}
