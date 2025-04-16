package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CommentResponse(
        @JsonProperty("owner") OwnerResponse owner,
        @JsonProperty("creation_date") long creationDate,
        @JsonProperty("comment_id") long commentId,
        @JsonProperty("body_markdown") String body) {}
