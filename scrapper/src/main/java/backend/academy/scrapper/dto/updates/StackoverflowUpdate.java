package backend.academy.scrapper.dto.updates;

import org.jspecify.annotations.Nullable;

public record StackoverflowUpdate(
    String user,
    long time,
    long questionId
) {
}
