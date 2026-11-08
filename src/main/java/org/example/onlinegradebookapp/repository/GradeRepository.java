package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long>, JpaSpecificationExecutor<Grade> {
    boolean existsByStudentIdAndTestId(Long studentId, Long testId);

    boolean existsByStudentIdAndTestIdAndIdNot(Long studentId, Long testId, Long id);

    Optional<Grade> findGradeByStudentIdAndTestId(Long studentId, Long testId);
}
