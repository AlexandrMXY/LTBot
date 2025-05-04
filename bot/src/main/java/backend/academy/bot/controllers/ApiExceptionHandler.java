package backend.academy.bot.controllers;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.api.model.responses.ApiErrorResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(NotFoundException exception) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), "404", exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> invalidRequest(InvalidRequestException exception) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "400", exception),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> internalError(Throwable t) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "500", t),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ApiErrorResponse> tooManyRequests(RequestNotPermitted exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
}
