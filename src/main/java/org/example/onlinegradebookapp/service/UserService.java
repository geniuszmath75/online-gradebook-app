package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.UserRegistrationDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolClassRepository classRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SchoolClassRepository classRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.classRepository = classRepository;
    }

    // Registers a new user using data from the registration DTO
    public void register(UserRegistrationDto dto) {
        // Check if user with given email already exists in DB
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email '" + dto.getEmail() + "' already exists");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUserRole(dto.getRole());

        // If classId is provided then add it to user entity
        if(dto.getClassId() != null) {
            SchoolClass schoolClass = classRepository.findById(dto.getClassId()).orElseThrow();
            user.setSchoolClass(schoolClass);
        }

        userRepository.save(user);
    }

    // Finds all users
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // Find a user with given ID
    public User findUserById(Long id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id = " + id +" not found"));
    }
}
