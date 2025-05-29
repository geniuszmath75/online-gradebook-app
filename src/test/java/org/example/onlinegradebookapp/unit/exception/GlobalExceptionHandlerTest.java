package org.example.onlinegradebookapp.unit.exception;

import org.example.onlinegradebookapp.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler handler;

    @BeforeEach
    public void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldReturnNotFound_whenResourceNotFoundException() {
        String message = "Resource not found";
        ResourceNotFoundException ex = new ResourceNotFoundException(message);

        ResponseEntity<ApiError> response = handler.handleResourceNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }

    @Test
    void shouldReturnUnauthorized_whenUnauthorizedException() {
        String message = "Unauthorized";
        UnauthorizedException ex = new UnauthorizedException(message);

        ResponseEntity<ApiError> response = handler.handleUnauthorized(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }

    @Test
    void shouldReturnBadRequest_whenBadRequestException() {
        String message = "Bad request";
        BadRequestException ex = new BadRequestException(message);

        ResponseEntity<ApiError> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo(message);
    }

    @Test
    void shouldReturnBadRequestWithValidationErrors_whenMethodArgumentNotValidException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("must not be blank");
    }

    @Test
    void shouldReturnBadRequestForDateFormat_whenHttpMessageNotReadableException_containsLocalDate() {
        HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Invalid LocalDate", null, httpInputMessage);

        ResponseEntity<ApiError> response = handler.handleInvalidFormat(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("Invalid date format");
    }

    @Test
    void shouldReturnBadRequestForEnumError_whenHttpMessageNotReadableException_hasInvalidFormatCause() {
        Throwable cause = new com.fasterxml.jackson.databind.exc.InvalidFormatException(null, "Invalid enum", "VALUE", String.class);
        HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Enum error", cause, httpInputMessage);

        ResponseEntity<ApiError> response = handler.handleInvalidFormat(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("Invalid enum");
    }

    @Test
    void shouldReturnBadRequestGeneric_whenHttpMessageNotReadableException_hasOtherCause() {
        Throwable cause = new RuntimeException("Other problem");
        HttpInputMessage httpInputMessage = mock(HttpInputMessage.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Something else", cause, httpInputMessage);

        ResponseEntity<ApiError> response = handler.handleInvalidFormat(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getMessage()).contains("Malformed request");
    }
}
