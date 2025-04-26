package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    // Check if class exists with given name
    Boolean existsByName(String name);
}
