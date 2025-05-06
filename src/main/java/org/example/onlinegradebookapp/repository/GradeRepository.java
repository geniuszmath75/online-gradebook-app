package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    // Check if grade exists with given student ID
    Boolean existsGradeByStudentId(Long studentId);

    // Check if grade exists with given test ID
    Boolean existsGradeByTestId(Long testId);
}
