package org.example.onlinegradebookapp.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Returns 401 with {@link org.example.onlinegradebookapp.exception.ApiError} when authentication is missing or invalid.
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ApiErrorResponseWriter errorWriter;

    public JsonAuthenticationEntryPoint(ApiErrorResponseWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorWriter.write(response, HttpStatus.UNAUTHORIZED, "Authentication required");
    }
}
