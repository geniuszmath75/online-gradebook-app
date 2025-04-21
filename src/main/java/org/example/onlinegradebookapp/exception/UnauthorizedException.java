package org.example.onlinegradebookapp.exception;

// Thrown when the user is not authorized to perform an action (HTTP 401)
public class UnauthorizedException extends RuntimeException{
    public UnauthorizedException(String message) {
        super(message);
    }
}
