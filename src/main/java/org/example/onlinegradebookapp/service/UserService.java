package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SchoolClassRepository classRepository, SubjectRepository subjectRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
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
            SchoolClass schoolClass = classRepository
                    .findById(dto.getClassId())
                    .orElseThrow(() -> new BadRequestException("Class with id=" + dto.getClassId() + " not found"));
            user.setSchoolClass(schoolClass);
        }

        // If subjects are provided then add them to user entity
        if(dto.getSubjects() != null) {
            List<Subject> subjects = findSubjectList(dto.getSubjects());
            user.setSubjects(subjects);
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

    // Update attributes of user with given ID
    public void updateUserAttributes(UserUpdateDto dto, Long id) throws ResourceNotFoundException {
        // Check if user with given email already exists
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email '"+dto.getEmail()+"' already exists");
        }

        // Find a user if exists in database
        Optional<User> optionalUser = userRepository.findById(id);

        // If the user exists, update it
        if(optionalUser.isPresent()) {
            User updatedUser = optionalUser.get();

            // Check if 'password' is given
            if(dto.getPassword() != null) {
                updatedUser.setPassword(passwordEncoder.encode(dto.getPassword()));
            }
            // Check if 'email' is given
            if(dto.getEmail() != null) {
                updatedUser.setEmail(dto.getEmail());
            }
            // Check if 'firstName' is given
            if(dto.getFirstName() != null) {
                updatedUser.setFirstName(dto.getFirstName());
            }
            // Check if 'lastName' is given
            if(dto.getLastName() != null) {
                updatedUser.setLastName(dto.getLastName());
            }
            // Check if 'role' is given
            if(dto.getRole() != null) {
                updatedUser.setUserRole(dto.getRole());
            }
            // Check if 'classId' is given
            if(dto.getClassId() != null) {
                SchoolClass updatedClass = classRepository
                        .findById(dto.getClassId())
                        .orElseThrow(() -> new BadRequestException("School class with id=" + dto.getClassId() + " not found"));
                updatedUser.setSchoolClass(updatedClass);
            }
            // Check if 'subjects' is given
            if(dto.getSubjects() != null) {
                List<Subject> updatedSubjects = findSubjectList(dto.getSubjects());
                updatedUser.setSubjects(updatedSubjects);
            }
            userRepository.save(updatedUser);
        } else {
            throw new ResourceNotFoundException("User with id=" + id + " not found");
        }
    }

    // Delete a user with given ID
    public void deleteUser(Long id) {
        if(userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("User with id=" + id + " not found");
        }
    }

    // Find list of subjects
    private List<Subject> findSubjectList(List<SubjectDto> subjectsList) throws BadRequestException {
        return subjectsList
                .stream()
                .map(subjectDto -> subjectRepository
                        .findByName(subjectDto.getName())
                        .orElseThrow(() -> new BadRequestException("Subject '" + subjectDto.getName() + "' not found"))
                )
                .collect(Collectors.toList());
    }
}
