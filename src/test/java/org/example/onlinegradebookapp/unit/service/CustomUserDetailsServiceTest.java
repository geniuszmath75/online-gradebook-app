package org.example.onlinegradebookapp.unit.service;

import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.example.onlinegradebookapp.security.CustomUserDetails;
import org.example.onlinegradebookapp.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomUserDetailsServiceTest {
    private UserRepository userRepository;
    private StudentRepository studentRepository;
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        studentRepository = Mockito.mock(StudentRepository.class);
        customUserDetailsService = new CustomUserDetailsService(userRepository, studentRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@gmail.com");
        user.setPassword("encoded-password");
        user.setRole(UserRole.ADMIN);

        when(userRepository.findByEmail("admin@gmail.com")).thenReturn(Optional.of(user));

        CustomUserDetails result = customUserDetailsService.loadUserByUsername("admin@gmail.com");

        assertEquals(1L, result.getId());
        assertEquals("admin@gmail.com", result.getUsername());
        assertEquals("encoded-password", result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN")));

        verify(userRepository).findByEmail("admin@gmail.com");
        verifyNoInteractions(studentRepository);
    }

    @Test
    void loadUserByUsername_shouldReturnStudentDetails_whenUserNotFoundButStudentExists() {
        Student student = new Student();
        student.setId(2L);
        student.setEmail("student@gmail.com");
        student.setPassword("student-pass");

        when(userRepository.findByEmail("student@gmail.com")).thenReturn(Optional.empty());
        when(studentRepository.findByEmail("student@gmail.com")).thenReturn(Optional.of(student));

        CustomUserDetails result = customUserDetailsService.loadUserByUsername("student@gmail.com");

        assertEquals(2L, result.getId());
        assertEquals("student@gmail.com", result.getUsername());
        assertEquals("student-pass", result.getPassword());
        assertTrue(result.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT")));

        verify(userRepository).findByEmail("student@gmail.com");
        verify(studentRepository).findByEmail("student@gmail.com");
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenEmailNotFoundInBothRepositories() {
        when(userRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());
        when(studentRepository.findByEmail("unknown@gmail.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown@gmail.com")
        );

        assertEquals("The email has been updated. Log in again to get a new token.", exception.getMessage());

        verify(userRepository).findByEmail("unknown@gmail.com");
        verify(studentRepository).findByEmail("unknown@gmail.com");
    }
}
