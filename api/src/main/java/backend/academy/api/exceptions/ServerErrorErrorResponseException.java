package backend.academy.api.exceptions;

import backend.academy.api.model.responses.ApiErrorResponse;

public class ServerErrorErrorResponseException extends ApiErrorResponseException {
    public ServerErrorErrorResponseException(ApiErrorResponse details, int code) {
        super(details, code);
    }
}
