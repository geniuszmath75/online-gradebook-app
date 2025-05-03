package org.example.onlinegradebookapp.repository;

import org.example.onlinegradebookapp.entity.KnowledgeTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeTestRepository extends JpaRepository<KnowledgeTest, Long> {
    // Check if knowledge test exists with given name
    Boolean existsByName(String name);
}
