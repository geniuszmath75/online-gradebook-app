package org.example.onlinegradebookapp.exception;

// Thrown when a request conflicts with current resource state or business rules (HTTP 409)
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
