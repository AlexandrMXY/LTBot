package backend.academy.api.model.requests;

import java.util.List;

public record AddLinkRequest(String link, List<String> tags, List<String> filters) {}
