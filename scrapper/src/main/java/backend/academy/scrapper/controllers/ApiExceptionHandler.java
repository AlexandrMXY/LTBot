package backend.academy.scrapper.controllers;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.api.model.responses.ApiErrorResponse;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(NotFoundException exception) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), "404", exception), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> alreadyExists(AlreadyExistsException exception) {
        return new ResponseEntity<>(new ApiErrorResponse("Already exists", "400", exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({InvalidRequestException.class, UnsupportedLinkException.class})
    public ResponseEntity<ApiErrorResponse> invalidRequest(Exception exception) {
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "400", exception),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> internalError(Throwable t) {
        log.atError()
                .setMessage("An unexpected internal error occurred")
                .setCause(t)
                .log();
        return new ResponseEntity<>(
                new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "500", t),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
