package backend.academy.scrapper.dto;

import backend.academy.api.model.AddLinkRequest;
import backend.academy.api.model.LinkResponse;
import backend.academy.scrapper.entities.TrackedLink;
import java.util.List;

public record LinkDto(String link, List<String> tags, List<String> filters, long id) {
    public LinkDto(AddLinkRequest request) {
        this(request.url(), request.tags(), request.filters(), 0);
    }

    public LinkDto(TrackedLink trackedLink) {
        this(trackedLink.url(), trackedLink.tags(), List.of(), trackedLink.id());
    }

    public LinkResponse asResponse() {
        return new LinkResponse(id, link, tags, filters);
    }
}
