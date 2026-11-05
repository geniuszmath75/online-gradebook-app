package org.example.onlinegradebookapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns 403 with {@link org.example.onlinegradebookapp.exception.ApiError} when the authenticated user lacks permission.
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final ApiErrorResponseWriter errorWriter;

    public JsonAccessDeniedHandler(ApiErrorResponseWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorWriter.write(response, HttpStatus.FORBIDDEN, "Access denied");
    }
}
