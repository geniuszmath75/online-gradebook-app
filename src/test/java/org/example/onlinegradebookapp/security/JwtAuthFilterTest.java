package org.example.onlinegradebookapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.example.onlinegradebookapp.exception.ApiError;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class JwtAuthFilterTest {
    private JwtAuthFilter jwtAuthFilter;
    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private FilterChain filterChain;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        userDetailsService = Mockito.mock(CustomUserDetailsService.class);
        filterChain = Mockito.mock(FilterChain.class);
        ApiErrorResponseWriter errorWriter = new ApiErrorResponseWriter(objectMapper);
        jwtAuthFilter = new JwtAuthFilter(jwtService, userDetailsService, errorWriter);
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipFilter_whenNoAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldSkipFilter_whenBearerTokenIsEmpty() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldAuthenticateUser_whenTokenIsValid() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        String email = "user@gmail.com";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                email,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(jwtService.extractUsername(jwt)).thenReturn(email);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.isTokenValid(jwt, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void shouldReturnUnauthorized_whenTokenExpired() throws ServletException, IOException {
        String jwt = "expired.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(jwt)).thenThrow(new ExpiredJwtException(null, null, "JWT expired"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(error.getStatus()).isEqualTo(401);
        assertThat(error.getMessage()).isEqualTo("JWT expired");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturnUnauthorized_whenTokenMalformed() throws ServletException, IOException {
        String jwt = "not-a-jwt";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(jwt))
                .thenThrow(new MalformedJwtException("JWT strings must contain exactly 2 period characters"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(error.getStatus()).isEqualTo(401);
        assertThat(error.getMessage()).isEqualTo("Invalid JWT token");
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldReturnUnauthorized_whenUsernameNotFound() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        String message = "The email has been updated. Log in again to get a new token.";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(jwt)).thenReturn("gone@example.com");
        when(userDetailsService.loadUserByUsername("gone@example.com"))
                .thenThrow(new UsernameNotFoundException(message));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(error.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldReturnNotFound_whenUserNotFound() throws ServletException, IOException {
        String jwt = "valid.jwt.token";
        String message = "User not found";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername(jwt)).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com"))
                .thenThrow(new ResourceNotFoundException(message));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(404);

        ApiError error = objectMapper.readValue(response.getContentAsString(), ApiError.class);
        assertThat(error.getStatus()).isEqualTo(404);
        assertThat(error.getMessage()).isEqualTo(message);
    }
}
