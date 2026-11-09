package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.SchoolClass;
import org.example.onlinegradebookapp.payload.response.SchoolClassResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    // Check if class exists with given name
    Boolean existsByName(String name);

    // Find the school class with given name
    Optional<SchoolClass> findByName(String name);

    @Query("""
            select new org.example.onlinegradebookapp.payload.response.SchoolClassResponse(
                schoolClass.id,
                schoolClass.name,
                schoolClass.createdAt,
                schoolClass.updatedAt
            )
            from SchoolClass schoolClass
            """)
    List<SchoolClassResponse> findAllResponses();

    @Query("""
            select new org.example.onlinegradebookapp.payload.response.SchoolClassResponse(
                schoolClass.id,
                schoolClass.name,
                schoolClass.createdAt,
                schoolClass.updatedAt
            )
            from SchoolClass schoolClass
            where schoolClass.id = :id
            """)
    Optional<SchoolClassResponse> findResponseById(Long id);
}
