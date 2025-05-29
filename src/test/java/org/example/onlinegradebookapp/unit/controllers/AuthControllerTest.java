package org.example.onlinegradebookapp.unit.controllers;

import org.example.onlinegradebookapp.controller.AuthController;
import org.example.onlinegradebookapp.exception.UnauthorizedException;
import org.example.onlinegradebookapp.payload.request.LoginDto;
import org.example.onlinegradebookapp.payload.request.StudentRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.example.onlinegradebookapp.security.CustomUserDetails;
import org.example.onlinegradebookapp.security.JwtService;
import org.example.onlinegradebookapp.service.CustomUserDetailsService;
import org.example.onlinegradebookapp.service.StudentService;
import org.example.onlinegradebookapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthControllerTest {
    private AuthController authController;
    private UserService userService;
    private StudentService studentService;
    private JwtService jwtService;
    private CustomUserDetailsService userDetailsService;
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        studentService = Mockito.mock(StudentService.class);
        userService = Mockito.mock(UserService.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);
        userDetailsService = Mockito.mock(CustomUserDetailsService.class);
        authController = new AuthController(userService,
                studentService, authenticationManager, jwtService, userDetailsService);
    }

    @Test
    void registerUser_ReturnsCreatedStatus() {
        UserRegistrationDto dto = new UserRegistrationDto();

        ResponseEntity<?> response = authController.registerUser(dto);

        verify(userService).register(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    void registerStudent_ReturnsCreatedStatus() {
        StudentRegistrationDto dto = new StudentRegistrationDto();

        ResponseEntity<?> response = authController.registerStudent(dto);

        verify(studentService).register(dto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Student registered successfully", response.getBody());
    }

    @Test
    void login_ReturnsToken_WhenCredentialsValid() throws UnauthorizedException {
        LoginDto dto = new LoginDto();
        dto.setEmail("email@gmail.com");
        dto.setPassword("password");
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mockToken");

        ResponseEntity<?> response = authController.login(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assert body != null;
        assertEquals(dto.getEmail(), body.get("email"));
        assertEquals("mockToken", body.get("token"));
    }

    @Test
    void login_ThrowsUnauthorizedException_WhenBadCredentials() {
        LoginDto dto = new LoginDto();
        dto.setEmail("email@gmail.com");
        dto.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(UnauthorizedException.class, () -> authController.login(dto));
    }
}
