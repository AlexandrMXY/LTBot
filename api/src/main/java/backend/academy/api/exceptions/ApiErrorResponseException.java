package backend.academy.api.exceptions;

import backend.academy.api.model.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ApiErrorResponseException extends RuntimeException {
    private final ApiErrorResponse details;

    public ApiErrorResponseException(ApiErrorResponse details) {
        this.details = details;
    }
}
