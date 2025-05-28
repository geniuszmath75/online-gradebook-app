package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.entity.Subject;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.exception.UnauthorizedException;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestDto;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestUpdateDto;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.SchoolClassRepository;
import org.example.onlinegradebookapp.repository.SubjectRepository;
import org.example.onlinegradebookapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class KnowledgeTestService {
    private final KnowledgeTestRepository knowledgeTestRepository;
    private final SchoolClassRepository classRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public KnowledgeTestService(KnowledgeTestRepository knowledgeTestRepository,
                                SchoolClassRepository classRepository,
                                SubjectRepository subjectRepository,
                                UserRepository userRepository, UserService userService) {
        this.knowledgeTestRepository = knowledgeTestRepository;
        this.classRepository = classRepository;
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    // Find all knowledge tests
    public List<KnowledgeTest> findAllKnowledgeTests() {
        return knowledgeTestRepository.findAll();
    }

    // Find knowledge test with given ID
    public KnowledgeTest findKnowledgeTestById(Long id) {
        return knowledgeTestRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge test with id = "+id+" not found"));
    }

    // Add new knowledge test with request DTO
    public void addKnowledgeTest(KnowledgeTestDto dto) {
        if(knowledgeTestRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Knowledge test '" + dto.getName() + "' already exists");
        }

        KnowledgeTest knowledgeTest = new KnowledgeTest();
        knowledgeTest.setName(dto.getName());
        knowledgeTest.setCategoryName(dto.getCategory());
        knowledgeTest.setTestDate(dto.getTestDate());

        // Check if class with given id exists and set it to entity
        SchoolClass schoolClass = classRepository
                .findById(dto.getClassId())
                .orElseThrow(() -> new BadRequestException("Class with id=" + dto.getClassId() + " not found"));
        knowledgeTest.setSchoolClass(schoolClass);

        // Check if subject with given id exists and set it to entity
        Subject subject = subjectRepository
                .findById(dto.getSubjectId())
                .orElseThrow(() -> new BadRequestException("Subject with id=" + dto.getSubjectId() + " not found"));
        knowledgeTest.setSubject(subject);

        // Get current user ID
        Long currentUserId = userService.getCurrentUserId();

        // Set logged user as test owner
        User teacher = userRepository
                .findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User with id=" + currentUserId + " not found"));
        knowledgeTest.setTeacher(teacher);

        knowledgeTestRepository.save(knowledgeTest);
    }

    // Update attributes of knowledge test with given ID
    public void updateKnowledgeTestAttributes(KnowledgeTestUpdateDto dto, Long id) throws ResourceNotFoundException {
        // Find a knowledge test if exists in database
        Optional<KnowledgeTest> optionalTest = knowledgeTestRepository.findById(id);

        // If the knowledge test exists, update it
        if(optionalTest.isPresent()) {
            KnowledgeTest test = optionalTest.get();

            // Get current user ID
            Long currentUserId = userService.getCurrentUserId();

            // Check if current user has ADMIN role
            boolean isAdmin = userService.hasRole(UserRole.ADMIN);

            // Only ADMIN or user who created a test can update it
            if(test.getTeacher().getId().equals(currentUserId) || isAdmin) {
                // Check if 'name' is given
                if(dto.getName() != null) {
                    test.setName(dto.getName());
                }
                // Check if 'category' is given
                if(dto.getCategory() != null) {
                    test.setCategoryName(dto.getCategory());
                }
                // Check if 'testDate' is given
                if(dto.getTestDate() != null) {
                    test.setTestDate(dto.getTestDate());
                }
                // Check if 'classId' is given
                if(dto.getClassId() != null) {
                    SchoolClass updatedClass = classRepository
                            .findById(dto.getClassId())
                            .orElseThrow(() -> new BadRequestException("School class with id=" + dto.getClassId() + " not found"));
                    test.setSchoolClass(updatedClass);
                }
                // Check if 'subjectId' is given
                if(dto.getSubjectId() != null) {
                    Subject updatedSubject = subjectRepository
                            .findById(dto.getSubjectId())
                            .orElseThrow(() -> new BadRequestException("Subject with id=" + dto.getSubjectId() + " not found"));
                    test.setSubject(updatedSubject);
                }
                knowledgeTestRepository.save(test);
            } else {
                throw new UnauthorizedException("You are not authorized to update knowledge test that you did not create");
            }
        } else {
            throw new ResourceNotFoundException("Knowledge test with id=" + id + " not found");
        }
    }

    // Delete a knowledge test with given ID
    public void deleteKnowledgeTest(Long id) {
        // Check if test exists with given ID
        KnowledgeTest test = knowledgeTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Knowledge test with id=" + id + " not found"));

        // Get current user ID
        Long currentUserId = userService.getCurrentUserId();

        // Check if user has ADMIN role
        boolean isAdmin = userService.hasRole(UserRole.ADMIN);

        // Only ADMIN or user who created test can delete it
        if(!test.getTeacher().getId().equals(currentUserId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this knowledge test");
        }

        knowledgeTestRepository.delete(test);
    }
}
