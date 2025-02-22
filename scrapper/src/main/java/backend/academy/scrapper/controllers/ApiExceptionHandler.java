package backend.academy.scrapper.controllers;

import backend.academy.api.exceptions.InvalidRequestException;
import backend.academy.api.model.ApiErrorResponse;
import backend.academy.api.exceptions.NotFoundException;
import backend.academy.scrapper.exceptions.AlreadyExistsException;
import backend.academy.scrapper.exceptions.UnsupportedLinkException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> notFound(NotFoundException exception) {
        return new ResponseEntity<>(
            new ApiErrorResponse(HttpStatus.NOT_FOUND.getReasonPhrase(), "404", exception),
            HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({InvalidRequestException.class, UnsupportedLinkException.class, AlreadyExistsException.class})
    public ResponseEntity<ApiErrorResponse> invalidRequest(Exception exception) {
        return new ResponseEntity<>(
            new ApiErrorResponse(HttpStatus.BAD_REQUEST.getReasonPhrase(), "400", exception),
            HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ApiErrorResponse> internalError(Throwable t) {
        log.error("An error occurred: {} {}", "",  t);
        return new ResponseEntity<>(
            new ApiErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), "500", t),
            HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
