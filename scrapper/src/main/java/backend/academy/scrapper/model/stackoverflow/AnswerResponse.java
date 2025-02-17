package backend.academy.scrapper.model.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnswerResponse(
    @JsonProperty("owner")
    OwnerResponse owner,
    @JsonProperty("creation_date")
    long creationDate,
    @JsonProperty("question_id")
    long questionId
) {
}
