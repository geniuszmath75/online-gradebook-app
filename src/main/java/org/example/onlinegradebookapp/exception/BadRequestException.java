package org.example.onlinegradebookapp.exception;

// Thrown when a request is malformed or contains invalid data (HTTP 400)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
