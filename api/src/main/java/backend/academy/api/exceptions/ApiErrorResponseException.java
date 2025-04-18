package backend.academy.api.exceptions;

import backend.academy.api.model.responses.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ApiErrorResponseException extends RuntimeException {
    private final ApiErrorResponse details;

    public ApiErrorResponseException(ApiErrorResponse details) {
        this.details = details;
    }

    @Override
    public String getMessage() {
        return details == null ? super.getMessage() : details.description();
    }
}
