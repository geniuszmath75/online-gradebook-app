package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.example.onlinegradebookapp.security.CustomUserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(email);

        // Returns user details for a regular user with assigned role
        if (user.isPresent()) {
            return new CustomUserDetails(
                    user.get().getId(),
                    user.get().getEmail(),
                    user.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.get().getRole()))
            );
        } else {
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("The email has been updated. Log in again to get a new token."));

            // Returns user details for a student with a hardcoded "STUDENT" role
            return new CustomUserDetails(
                    student.getId(),
                    student.getEmail(),
                    student.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + "STUDENT"))
            );
        }
    }
}
