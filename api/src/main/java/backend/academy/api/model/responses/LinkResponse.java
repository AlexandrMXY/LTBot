package backend.academy.api.model.responses;

import java.util.List;

public record LinkResponse(long id, String url, List<String> tags, List<String> filters) {}
