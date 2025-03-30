package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record CommentsResponse(
        List<CommentResponse> items,
        @JsonProperty("has_more") boolean hasMore,
        @JsonProperty("quota_max") int quotaMax,
        @JsonProperty("quota_remaining") int quotaRemaining) {}
