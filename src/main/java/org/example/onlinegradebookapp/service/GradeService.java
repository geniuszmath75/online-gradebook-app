package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.event.GradeCreatedEvent;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ConflictException;
import org.example.onlinegradebookapp.exception.ForbiddenException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.payload.request.GradeDto;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.repository.GradeRepository;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.example.onlinegradebookapp.specification.GradeSpecifications;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final KnowledgeTestRepository knowledgeTestRepository;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        KnowledgeTestRepository knowledgeTestRepository,
                        UserService userService,
                        ApplicationEventPublisher eventPublisher) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.knowledgeTestRepository = knowledgeTestRepository;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
    }

    // Finds students' grades with optional filters and pagination
    public Page<Grade> findAllGrades(Long studentId, Long testId,
                                     Instant createdFrom, Instant createdTo,
                                     Pageable pageable) {
        if (createdFrom != null && createdTo != null && createdFrom.isAfter(createdTo)) {
            throw new BadRequestException("'createdFrom' must be before or equal to 'createdTo'");
        }
        boolean isAdmin = userService.hasRole(UserRole.ADMIN);
        Long teacherId = isAdmin ? null : userService.getCurrentUserId();
        return gradeRepository.findAll(
                GradeSpecifications.withFilters(studentId, testId, createdFrom, createdTo, teacherId),
                pageable
        );
    }

    // Finds a grade with given ID
    public Grade findGradeById(Long id) {
        Grade grade = gradeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade with id=" + id + " not found"));

        assertCanManageGrade(grade);
        return grade;
    }

    // Add a new grade with request DTO
    public Grade addGrade(GradeDto dto) {
        // If student exists, then add it to entity; otherwise throw error 400
        Student student = studentRepository
                .findById(dto.getStudentId())
                .orElseThrow(() -> new BadRequestException("Student with id=" + dto.getStudentId() + " not found"));

        // If test exists, then add it to entity; otherwise throw error 400
        KnowledgeTest test = knowledgeTestRepository
                .findById(dto.getTestId())
                .orElseThrow(() -> new BadRequestException("Test with id=" + dto.getTestId() + " not found"));

        // Get current user ID
        Long currentUserId = userService.getCurrentUserId();

        // Check if current user has ADMIN role
        boolean isAdmin = userService.hasRole(UserRole.ADMIN);

        if(test.getTeacher().getId().equals(currentUserId) || isAdmin) {
            dto.setTestId(test.getId());
        } else {
            throw new ForbiddenException("You are not authorized to add grade to a test that you did not create");
        }

        // Check for duplicates after authorization to avoid exposing another teacher's data.
        if (gradeRepository.existsByStudentIdAndTestId(dto.getStudentId(), dto.getTestId())) {
            throw new ConflictException("Student with id=" + dto.getStudentId()
                    + " has already a grade for test with id=" + dto.getTestId());
        }

        Grade grade = new Grade();
        grade.setGrade(dto.getGrade());
        grade.setStudent(student);
        grade.setTest(test);

        if(dto.getDescription() != null) {
            grade.setDescription(dto.getDescription());
        }

        Grade saved = gradeRepository.save(grade);
        eventPublisher.publishEvent(new GradeCreatedEvent(
                saved.getId(),
                saved.getStudent().getId(),
                saved.getTest().getId(),
                saved.getGrade(),
                currentUserId
        ));
        return saved;
    }

    // Update attributes of grade with given ID
    public Grade updateGradeAttributes(GradeUpdateDto dto, Long id) throws ResourceNotFoundException {
        // Find a grade if exists in database
        Optional<Grade> optionalGrade = gradeRepository.findById(id);

        // Checks if grade exists
        if(optionalGrade.isPresent()) {
            Grade grade = optionalGrade.get();

            // Get current user ID
            Long currentUserId = userService.getCurrentUserId();

            // Check if current user has ADMIN role
            boolean isAdmin = userService.hasRole(UserRole.ADMIN);

            // Only ADMIN or user who created a test can update the grade assigned to it
            if (!isAdmin && isOwnedBy(grade, currentUserId)) {
                throw new ForbiddenException("You are not authorized to update this grade");
            }

            Student updatedStudent = grade.getStudent();
            KnowledgeTest updatedTest = grade.getTest();

            if (dto.getStudentId() != null) {
                updatedStudent = studentRepository
                        .findById(dto.getStudentId())
                        .orElseThrow(() -> new BadRequestException("Student with id=" + dto.getStudentId() + " not found"));
            }
            if (dto.getTestId() != null) {
                updatedTest = knowledgeTestRepository
                        .findById(dto.getTestId())
                        .orElseThrow(() -> new BadRequestException("Knowledge test with id=" + dto.getTestId() + " not found"));
            }

            if (!isAdmin && !isOwnedBy(updatedTest, currentUserId)) {
                throw new ForbiddenException("You are not authorized to assign a grade to this test");
            }

            if (gradeRepository.existsByStudentIdAndTestIdAndIdNot(
                    updatedStudent.getId(), updatedTest.getId(), id)) {
                throw new ConflictException("Student with id=" + updatedStudent.getId()
                        + " has already a grade for test with id=" + updatedTest.getId());
            }

            // Check if 'grade' is given
            if(dto.getGrade() != null) {
                grade.setGrade(dto.getGrade());
            }
            // Check if 'description' is given
            if(dto.getDescription() != null) {
                grade.setDescription(dto.getDescription());
            }
            grade.setStudent(updatedStudent);
            grade.setTest(updatedTest);
            return gradeRepository.save(grade);
        } else {
            throw new ResourceNotFoundException("Grade with id=" + id + " not found");
        }
    }

    // Delete a grade with given ID
    public void deleteGrade(Long id) {
        // Check if test exists with given ID
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade with id=" + id + " not found"));

        // Get current user ID
        Long currentUserId = userService.getCurrentUserId();

        // Check if user has ADMIN role
        boolean isAdmin = userService.hasRole(UserRole.ADMIN);

        // Only ADMIN or user who created grade can delete it
        if(!isAdmin && isOwnedBy(grade, currentUserId)) {
            throw new ForbiddenException("You are not authorized to delete this grade");
        }

        gradeRepository.deleteById(id);
    }

    private void assertCanManageGrade(Grade grade) {
        Long currentUserId = userService.getCurrentUserId();
        boolean isAdmin = userService.hasRole(UserRole.ADMIN);
        if (!isAdmin && isOwnedBy(grade, currentUserId)) {
            throw new ForbiddenException("You are not authorized to view this grade");
        }
    }

    private boolean isOwnedBy(Grade grade, Long userId) {
        return grade == null
                || grade.getTest() == null
                || grade.getTest().getTeacher() == null
                || userId == null
                || !userId.equals(grade.getTest().getTeacher().getId());
    }

    private boolean isOwnedBy(KnowledgeTest test, Long userId) {
        return test != null
                && test.getTeacher() != null
                && userId != null
                && userId.equals(test.getTeacher().getId());
    }
}
