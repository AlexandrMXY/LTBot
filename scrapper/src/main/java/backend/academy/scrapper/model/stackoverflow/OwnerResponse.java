package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OwnerResponse(
    @JsonProperty("user_id")
    long userId,
    @JsonProperty("display_name")
    String displayName
) {
}
