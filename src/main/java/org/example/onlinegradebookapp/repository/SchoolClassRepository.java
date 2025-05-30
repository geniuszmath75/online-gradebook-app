package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    // Check if class exists with given name
    Boolean existsByName(String name);

    // Find the school class with given name
    Optional<SchoolClass> findByName(String name);
}
