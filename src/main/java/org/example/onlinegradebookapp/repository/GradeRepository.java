package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    // Check if grade exists with given student ID
    Boolean existsGradeByStudentId(Long studentId);

    // Check if grade exists with given test ID
    Boolean existsGradeByTestId(Long testId);

    // Find the grade with given studentId and testId
    Optional<Grade> findGradeByStudentIdAndTestId(Long studentId, Long testId);
}
