package org.example.onlinegradebookapp.exception;

// Thrown when a requested resource is not found (HTTP 404)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
