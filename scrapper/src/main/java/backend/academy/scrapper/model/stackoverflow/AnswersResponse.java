package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AnswersResponse(
        @JsonProperty("items") List<AnswerResponse> items,
        @JsonProperty("has_more") boolean hasMore,
        @JsonProperty("quota_max") int quotaMax,
        @JsonProperty("quota_remaining") int quotaRemaining) {}
