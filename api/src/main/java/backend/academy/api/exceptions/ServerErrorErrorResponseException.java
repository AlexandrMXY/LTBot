package backend.academy.api.exceptions;

import backend.academy.api.model.responses.ApiErrorResponse;

public class ServerErrorErrorResponseException extends ErrorResponseException {
    public ServerErrorErrorResponseException(ApiErrorResponse details, int code) {
        super(details, code);
    }
}
