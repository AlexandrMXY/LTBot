package backend.academy.api.exceptions;

import backend.academy.api.model.ApiErrorResponse;
import lombok.Getter;
import lombok.experimental.StandardException;

@Getter
public class ErrorResponseException extends RuntimeException {
    private final ApiErrorResponse details;

    public ErrorResponseException(ApiErrorResponse details) {
        this.details = details;
    }
}
