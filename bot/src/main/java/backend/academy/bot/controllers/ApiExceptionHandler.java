package backend.academy.bot.controllers;

import backend.academy.bot.model.ApiErrorResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({Exception.class})
    public ResponseEntity<?> handleException(Exception e) {
        return new ResponseEntity<>(new ApiErrorResponse(
            "Error", "Error", e
        ), HttpStatusCode.valueOf(400));
    }
}
