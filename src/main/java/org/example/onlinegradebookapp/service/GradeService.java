package org.example.onlinegradebookapp.service;

import org.example.onlinegradebookapp.entity.Grade;
import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.example.onlinegradebookapp.entity.Student;
import org.example.onlinegradebookapp.exception.BadRequestException;
import org.example.onlinegradebookapp.payload.GradeDto;
import org.example.onlinegradebookapp.repository.GradeRepository;
import org.example.onlinegradebookapp.repository.KnowledgeTestRepository;
import org.example.onlinegradebookapp.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final KnowledgeTestRepository knowledgeTestRepository;

    public GradeService(GradeRepository gradeRepository,
                        StudentRepository studentRepository,
                        KnowledgeTestRepository knowledgeTestRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.knowledgeTestRepository = knowledgeTestRepository;
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
        grade.setTest(test);

        gradeRepository.save(grade);
    }
}
