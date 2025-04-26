package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public CustomUserDetailsService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    // Loads a user by email from either the user or student repository
    @Override
    public UserDetails loadUserByUsername(String email) throws ResourceNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isPresent()) {
            // Returns user details for a regular user with assigned role
            return new org.springframework.security.core.userdetails.User(
                    user.get().getEmail(),
                    user.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.get().getRole()))
            );
        }

        Optional<Student> student = studentRepository.findByEmail(email);
        if(student.isPresent()) {
            // Returns user details for a student with a hardcoded "STUDENT" role
            return new org.springframework.security.core.userdetails.User(
                    student.get().getEmail(),
                    student.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + "STUDENT"))
            );
        }

        throw new ResourceNotFoundException("User not found with email: " + email);
    }
}
