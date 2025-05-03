package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Boolean existsByName(String name);
    Optional<Subject> findByName(String name);
}
