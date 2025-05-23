package org.example.onlinegradebookapp.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    // Configures the authentication manager using the provided authentication configuration
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Creates a password encoder bean using the BCrypt algorithm
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configures the Spring Security filter chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disables CSRF protection
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Allows public access to swagger docs
                        .requestMatchers("/api/auth/**").permitAll() // Allows public access to authentication endpoints
                        // Role-based endpoints
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasAnyRole("TEACHER")
                        .requestMatchers("/api/students/**").hasRole("STUDENT")
                        .requestMatchers("/api/classes/**").hasRole("TEACHER")
                        .requestMatchers("/api/subjects/**").hasRole("TEACHER")
                        .requestMatchers("/api/knowledge_tests/**").hasRole("TEACHER")
                        .requestMatchers("/api/grades/**").hasRole("TEACHER")
                        .anyRequest().authenticated() // Requires authentication for all other endpoints
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless session (required for JWT)
                .formLogin(AbstractHttpConfigurer::disable) // Disables default form login
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter before Spring Security's default filter


        return http.build();
    }
}
