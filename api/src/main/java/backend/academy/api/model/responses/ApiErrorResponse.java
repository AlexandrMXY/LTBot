package backend.academy.api.model.responses;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record ApiErrorResponse(
        String description, String code, String exceptionName, String exceptionMessage, List<String> stacktrace) {
    public ApiErrorResponse(String description, String code, Throwable exception) {
        this(
                description,
                code,
                exception.getClass().getName(),
                exception.getMessage(),
                Arrays.stream(exception.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.toList()));
    }
}
