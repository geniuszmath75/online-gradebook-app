package org.example.onlinegradebookapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,
            precision = 2,
            scale = 1,
            columnDefinition = "numeric(2, 1) CHECK (grade >= 1 AND grade <= 6)")
    private BigDecimal grade;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "test_id", nullable = false)
    private KnowledgeTest test;
}
