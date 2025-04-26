package backend.academy.api.exceptions;

import backend.academy.api.model.responses.ApiErrorResponse;
import lombok.Getter;

@Getter
public class ApiErrorResponseException extends RuntimeException {
    private final ApiErrorResponse details;
    private final int code;

    public ApiErrorResponseException(ApiErrorResponse details, int code) {
        this.details = details;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return details == null ? super.getMessage() : details.description();
    }
}
