package org.example.onlinegradebookapp.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handles ResourceNotFoundException and returns 404 Not Found response
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Handles UnauthorizedException and returns 401 Unauthorized response
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException ex) {
        ApiError error = new ApiError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // Handles BadRequestException and returns 400 Bad Request response
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handles validation errors and returns a detailed 400 Bad Request response
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        StringBuilder errorMessage = new StringBuilder("Validation error: ");
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                errorMessage.append(fieldError.getDefaultMessage()).append("; ")
        );

        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.value(), errorMessage.toString());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handles invalid format errors(LocalDate, enums) and returns a detailed 400 Bad Request Exception
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidFormat(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof DateTimeParseException || ex.getMessage().contains("LocalDate")) {
            ApiError error = new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    "Invalid date format. Expected format is YYYY-MM-DD."
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
        if(cause instanceof InvalidFormatException) {
            ApiError error = new ApiError(
                    HttpStatus.BAD_REQUEST.value(),
                    cause.getMessage()
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed request. Please check your request body."
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}
