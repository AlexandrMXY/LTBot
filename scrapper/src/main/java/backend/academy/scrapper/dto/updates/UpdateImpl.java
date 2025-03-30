package backend.academy.scrapper.dto.updates;

import backend.academy.api.model.LinkUpdate;

public record UpdateImpl(long user, long date, String url, String preview, String author) implements Update {
    @Override
    public LinkUpdate createRequest() {
        return new LinkUpdate(user, date, url, preview, author);
    }
}
