package org.example.onlinegradebookapp.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiError {
    private int status;
    private String message;

    public ApiError() {
        this.status = 500;
        this.message = "Internal Server Error";
    }

    // Represents a custom error response with HTTP status code and message
    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
