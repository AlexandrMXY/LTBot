package backend.academy.scrapper.dto.updates;

import backend.academy.api.model.LinkUpdate;
import lombok.RequiredArgsConstructor;

public record Update(long user, long date, String url, String preview, String author, String type) {
    public LinkUpdate createRequest() {
        return new LinkUpdate(user, date, url, preview, author, type);
    }

    public Update(long user, long date, String url, String preview, String author, Types type) {
        this(user, date, url, preview, author, type.value);
    }

    @RequiredArgsConstructor
    public enum Types {
        COMMENT(LinkUpdate.Types.COMMENT),
        ANSWER(LinkUpdate.Types.ANSWER),
        ISSUE(LinkUpdate.Types.ISSUE),
        PULL_REQUEST(LinkUpdate.Types.PULL_REQUEST);

        public final String value;
    }
}
