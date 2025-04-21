package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    // Find the student with given email
    Optional<Student> findByEmail(String email);

    // Check if the student exists with given email
    Boolean existsByEmail(String email);
}
