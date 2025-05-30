package org.example.onlinegradebookapp.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "subjects")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToMany(mappedBy = "subjects", cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonManagedReference
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<KnowledgeTest> tests = new ArrayList<>();
}
