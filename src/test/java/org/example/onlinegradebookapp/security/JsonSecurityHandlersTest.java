package org.example.onlinegradebookapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.onlinegradebookapp.exception.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSecurityHandlersTest {
    private JsonAuthenticationEntryPoint entryPoint;
    private JsonAccessDeniedHandler accessDeniedHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ApiErrorResponseWriter writer = new ApiErrorResponseWriter(objectMapper);
        entryPoint = new JsonAuthenticationEntryPoint(writer);
        accessDeniedHandler = new JsonAccessDeniedHandler(writer);
    }

    @Test
    void entryPoint_shouldWriteUnauthorizedApiError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new InsufficientAuthenticationException("Full authentication is required")
        );

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");
        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(error.getStatus()).isEqualTo(401);
        assertThat(error.getMessage()).isEqualTo("Authentication required");
    }

    @Test
    void accessDeniedHandler_shouldWriteForbiddenApiError() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        accessDeniedHandler.handle(
                new MockHttpServletRequest(),
                response,
                new AccessDeniedException("Forbidden")
        );

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");
        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(error.getStatus()).isEqualTo(403);
        assertThat(error.getMessage()).isEqualTo("Access denied");
    }
}
