package backend.academy.api.exceptions;

import backend.academy.api.model.responses.ApiErrorResponse;

public class BadRequestErrorResponseException extends ApiErrorResponseException {
    public BadRequestErrorResponseException(ApiErrorResponse details, int code) {
        super(details, code);
    }
}
