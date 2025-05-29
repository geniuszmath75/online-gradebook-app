package org.example.onlinegradebookapp.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JwtServiceTest {
    private JwtService jwtService;
    private CustomUserDetails userDetails;

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
        userDetails = new CustomUserDetails(
                1L,
                "student@gmail.com",
                "encodedPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    @Test
    void generateToken_shouldReturnValidJwt() {
        String token = jwtService.generateToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_shouldReturnCorrectEmail() {
        String token = jwtService.generateToken(userDetails);
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(userDetails.getUsername());
    }

    @Test
    void extractExpiration_shouldReturnFutureDate() {
        String token = jwtService.generateToken(userDetails);
        Date expiration = jwtService.extractExpiration(token);
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken(userDetails);
        boolean valid = jwtService.isTokenValid(token, userDetails);
        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidUser() {
        String token = jwtService.generateToken(userDetails);

        CustomUserDetails anotherUser = new CustomUserDetails(
                2L,
                "hacker@gmail.com",
                "differentPassword",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );

        boolean valid = jwtService.isTokenValid(token, anotherUser);
        assertThat(valid).isFalse();
    }

    @Test
    void extractClaim_shouldExtractSubjectUsingLambda() {
        String token = jwtService.generateToken(userDetails);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        assertThat(subject).isEqualTo(userDetails.getUsername());
    }
}
