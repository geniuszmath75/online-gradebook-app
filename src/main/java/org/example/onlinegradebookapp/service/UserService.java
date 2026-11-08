package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ConflictException;
import org.example.onlinegradebookapp.exception.ForbiddenException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserUpdateDto;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.example.onlinegradebookapp.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final KnowledgeTestRepository knowledgeTestRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       SchoolClassRepository classRepository,
                       SubjectRepository subjectRepository,
                       KnowledgeTestRepository knowledgeTestRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.knowledgeTestRepository = knowledgeTestRepository;
    }

    // Registers a new user using data from the registration DTO
    public User register(UserRegistrationDto dto) {
        if (dto.getRole() == null) {
            throw new BadRequestException("Role cannot be null");
        }

        // Check if user with given email already exists in DB
        if(userRepository.existsByEmail(dto.getEmail())) {
            throw new ConflictException("Email '" + dto.getEmail() + "' already exists");
        }
        // Check if ADMIN role can be assigned
        if(dto.getRole().equals(UserRole.ADMIN)) {
            Long adminCount = userRepository.countByRole(UserRole.ADMIN);
            if(adminCount > 0) {
                throw new ConflictException("There can only be one ADMIN user.");
            }
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

            if (userRepository.existsBySchoolClassId(dto.getClassId())) {
                throw new ConflictException(
                        "Class with id=" + dto.getClassId() + " already has an assigned teacher"
                );
            }

            user.setSchoolClass(schoolClass);
        }

        // If subjects are provided then add them to user entity
        if(dto.getSubjects() != null) {
            List<Subject> subjects = findSubjectList(dto.getSubjects());
            user.setSubjects(subjects);
        }
        return userRepository.save(user);
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
    public User updateUserAttributes(UserUpdateDto dto, Long id) throws ResourceNotFoundException {
        Optional<User> optionalUser = userRepository.findById(id);

        if(optionalUser.isPresent()) {
            User updatedUser = optionalUser.get();

            // Conflict only when email changes to one already used by someone else
            if(dto.getEmail() != null
                    && !dto.getEmail().equals(updatedUser.getEmail())
                    && Boolean.TRUE.equals(userRepository.existsByEmail(dto.getEmail()))) {
                throw new ConflictException("Email '"+dto.getEmail()+"' already exists");
            }

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
                if (!dto.getRole().equals(updatedUser.getRole()) && !hasRole(UserRole.ADMIN)) {
                    throw new ForbiddenException("Only ADMIN users can change user roles.");
                }

                // Check if ADMIN role can be assigned
                if(dto.getRole().equals(UserRole.ADMIN)
                        && !UserRole.ADMIN.equals(updatedUser.getRole())) {
                    Long adminCount = userRepository.countByRole(UserRole.ADMIN);
                    if(adminCount > 0) {
                        throw new ConflictException("There can only be one ADMIN user.");
                    }
                }
                updatedUser.setUserRole(dto.getRole());
            }
            // Check if 'classId' is given
            if(dto.getClassId() != null) {
                // Check if class exists
                SchoolClass updatedClass = classRepository
                        .findById(dto.getClassId())
                        .orElseThrow(() -> new BadRequestException("School class with id=" + dto.getClassId() + " not found"));

                // Detach old user if assigned
                if(updatedClass.getTeacher() != null) {
                    User oldUser = updatedClass.getTeacher();
                    oldUser.setSchoolClass(null);
                    userRepository.save(oldUser);
                }

                // Assign new user to class
                updatedUser.setSchoolClass(updatedClass);
            }
            // Check if 'subjects' is given
            if(dto.getSubjects() != null) {
                List<Subject> updatedSubjects = findSubjectList(dto.getSubjects());
                updatedUser.setSubjects(updatedSubjects);
            }
            return userRepository.save(updatedUser);
        } else {
            throw new ResourceNotFoundException("User with id=" + id + " not found");
        }
    }

    // Delete a user with given ID
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id=" + id + " not found"));

        // 1) Delete owned knowledge tests first (grades cascade from KnowledgeTest)
        List<KnowledgeTest> ownedTests = new ArrayList<>(user.getTests());
        if (!ownedTests.isEmpty()) {
            knowledgeTestRepository.deleteAll(ownedTests);
            user.getTests().clear();
        }

        // 2) Break bidirectional OneToOne with school class (owning + inverse side)
        SchoolClass schoolClass = user.getSchoolClass();
        if (schoolClass != null) {
            schoolClass.setTeacher(null);
            user.setSchoolClass(null);
        }

        // 3) Break ManyToMany with subjects on both sides, then drop join rows
        for (Subject subject : new ArrayList<>(user.getSubjects())) {
            subject.getUsers().remove(user);
        }
        user.getSubjects().clear();

        userRepository.delete(user);
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

    // Get logged user ID
    public Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }

    public boolean hasRole(UserRole role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role));
    }

}
