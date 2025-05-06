package org.example.onlinegradebookapp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.onlinegradebookapp.exception.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    // Filter used once per HTTP request
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // No auth header or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from header
        jwt = authHeader.substring(7);

        try {
            // Extract email from token
            userEmail = jwtService.extractUsername(jwt);

            // Check if user/student hasn't authenticated yet
            if(SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user/student data
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Token validation
                if(jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authorization token in Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authorization in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            // Pass the request further in the filter chain
            filterChain.doFilter(request, response);
        } catch(ExpiredJwtException ex) {
            // Return 401 error if token expired
            ObjectMapper mapper = new ObjectMapper();
            ApiError error = new ApiError(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // Set status code
            response.setContentType("application/json"); // Set content type
            mapper.writeValue(response.getWriter(), error); // Set response body
        }
    }
}
