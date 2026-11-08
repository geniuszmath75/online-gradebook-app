package org.example.onlinegradebookapp.exception;

/**
 * Thrown when an authenticated user is not allowed to perform an action.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
