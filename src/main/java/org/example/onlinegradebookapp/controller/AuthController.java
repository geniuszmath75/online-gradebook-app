package org.example.onlinegradebookapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.onlinegradebookapp.exception.UnauthorizedException;
import org.example.onlinegradebookapp.payload.request.LoginDto;
import org.example.onlinegradebookapp.payload.request.StudentRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.example.onlinegradebookapp.security.CustomUserDetails;
import org.example.onlinegradebookapp.security.JwtService;
import org.example.onlinegradebookapp.service.CustomUserDetailsService;
import org.example.onlinegradebookapp.service.StudentService;
import org.example.onlinegradebookapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authorization", description = "Registration and log in operations to authorize USERS/STUDENTS")
@SecurityRequirements()
public class AuthController {
    private final UserService userService;
    private final StudentService studentService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(UserService userService, StudentService studentService, AuthenticationManager authenticationManager,
                          JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.userService = userService;
        this.studentService = studentService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register/user")
    @Operation(summary = "Register a new user(TEACHER)",
    description = "Create a new user in database")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto dto) {
        userService.register(dto);

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/register/student")
    @Operation(summary = "Register a new student",
            description = "Create a new student in database")
    public ResponseEntity<?> registerStudent(@Valid @RequestBody StudentRegistrationDto dto) {
        studentService.register(dto);
        return new ResponseEntity<>("Student registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login a user/student with JWT token",
            description = "Generates JWT token used to authorize other endpoints")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto dto) throws UnauthorizedException {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
            CustomUserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
            String token = jwtService.generateToken(userDetails);
            Map<String, String> response = new HashMap<>();
            response.put("email", dto.getEmail());
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}
