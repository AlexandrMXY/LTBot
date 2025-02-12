package backend.academy.api.exceptions;


import lombok.experimental.StandardException;
import java.util.Objects;


public class ErrorResponseException extends RuntimeException {
    public ErrorResponseException() {
    }

    public ErrorResponseException(String message) {
        super(message);
    }

    public ErrorResponseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorResponseException(Throwable cause) {
        super(cause);
    }

    public ErrorResponseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
