package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.exception.ResourceNotFoundException;
import org.example.onlinegradebookapp.exception.UnauthorizedException;
import org.example.onlinegradebookapp.payload.request.GradeDto;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.repository.GradeRepository;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final KnowledgeTestRepository knowledgeTestRepository;
    private final UserService userService;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        KnowledgeTestRepository knowledgeTestRepository, UserService userService) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.knowledgeTestRepository = knowledgeTestRepository;
        this.userService = userService;
    }

    // Finds all students' grades
    public List<Grade> findAllGrades() {
        return gradeRepository.findAll();
    }

    // Finds a grade with given ID
    public Grade findGradeById(Long id) {
        return gradeRepository
                .findById(id)
                .orElseThrow(() -> new BadRequestException("Grade with id=" + id + " not found"));
    }

    // Add a new grade with request DTO
    public void addGrade(GradeDto dto) {
        // Check if a student has already a grade for given test
        if(gradeRepository.existsGradeByStudentId(dto.getStudentId()) &&
        gradeRepository.existsGradeByTestId(dto.getTestId())) {
            throw new BadRequestException("Student with id=" + dto.getStudentId() + " has already a grade for test with id=" + dto.getTestId());
        }

        Grade grade = new Grade();
        grade.setGrade(dto.getGrade());

        // If description is provided then add it to entity
        if(dto.getDescription() != null) {
            grade.setDescription(dto.getDescription());
        }

        // If student exists, then add it to entity; otherwise throw error 400
        Student student = studentRepository
                .findById(dto.getStudentId())
                .orElseThrow(() -> new BadRequestException("Student with id=" + dto.getStudentId() + " not found"));
        grade.setStudent(student);

        // If test exists, then add it to entity; otherwise throw error 400
        KnowledgeTest test = knowledgeTestRepository
                .findById(dto.getTestId())
                .orElseThrow(() -> new BadRequestException("Test with id=" + dto.getTestId() + " not found"));

        // Get current user ID
        Long currentUserId = userService.getCurrentUserId();

        // Check if current user has ADMIN role
        boolean isAdmin = userService.hasRole(UserRole.ADMIN);

        if(test.getTeacher().getId().equals(currentUserId) || isAdmin) {
            grade.setTest(test);
        } else {
            throw new UnauthorizedException("You are not authorized to add grade to test that you did not create");
        }

        gradeRepository.save(grade);
    }

    // Update attributes of grade with given ID
    public void updateGradeAttributes(GradeUpdateDto dto, Long id) throws ResourceNotFoundException {
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
            if(grade.getTest().getTeacher().getId().equals(currentUserId) || isAdmin) {
                // Check if 'grade' is given
                if(dto.getGrade() != null) {
                    grade.setGrade(dto.getGrade());
                }
                // Check if 'description' is given
                if(dto.getDescription() != null) {
                    grade.setDescription(dto.getDescription());
                }
                // Check if 'studentId' is given
                if(dto.getStudentId() != null) {
                    Student updatedStudent = studentRepository
                            .findById(dto.getStudentId())
                            .orElseThrow(() -> new BadRequestException("Student with id=" + dto.getStudentId() + " not found"));
                    grade.setStudent(updatedStudent);
                }
                // Check if 'testId' is given
                if(dto.getTestId() != null) {
                    KnowledgeTest updatedTest = knowledgeTestRepository
                            .findById(dto.getTestId())
                            .orElseThrow(() -> new BadRequestException("Knowledge test with id=" + dto.getTestId() + " not found"));
                    grade.setTest(updatedTest);
                }
                gradeRepository.save(grade);
            } else {
                throw new UnauthorizedException("You are not authorized update the grade assigned to test that you did not create");
            }
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
        if(!grade.getTest().getTeacher().getId().equals(currentUserId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this knowledge test");
        }

        gradeRepository.deleteById(id);
    }
}
