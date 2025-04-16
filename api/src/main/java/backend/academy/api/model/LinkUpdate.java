package backend.academy.api.model;

import lombok.experimental.UtilityClass;

public record LinkUpdate(long chatId, long time, String url, String content, String author, String type) {
    @UtilityClass
    public static class Types {
        public static final String COMMENT = "comment";
        public static final String ANSWER = "answer";
        public static final String ISSUE = "issue";
        public static final String PULL_REQUEST = "pull_request";
    }
}
