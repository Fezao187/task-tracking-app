package com.katlego.task_tracking_api.exceptionHandler;

import com.katlego.task_tracking_api.exception.EmailAlreadyExistsException;
import com.katlego.task_tracking_api.exception.ResourceAlreadyExistException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse createErrorResponse(String message, HttpStatusCode status) {
        return new ErrorResponse(
                message,
                status.value(),
                status,
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(ResourceAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistException(ResourceAlreadyExistException ex) {
        return new ResponseEntity<>(
                createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        logger.error("Unhandled exception caught: ", ex);
        return new ResponseEntity<>(
                createErrorResponse("An unexpected error occurred. Please try again later", HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
