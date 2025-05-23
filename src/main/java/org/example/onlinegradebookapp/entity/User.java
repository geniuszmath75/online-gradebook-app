package org.example.onlinegradebookapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.onlinegradebookapp.entity.UserRole.AdminRole;
import org.example.onlinegradebookapp.entity.UserRole.RoleName;
import org.example.onlinegradebookapp.entity.UserRole.TeacherRole;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "firstname", nullable = false)
    private String firstName;

    @Column(name = "lastname",nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Transient
    @JsonIgnore
    private RoleName roleName;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;

    @ManyToMany
    @JsonBackReference
    @JoinTable(
            name = "teachers_subjects",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<KnowledgeTest> tests = new ArrayList<>();

    public void setUserRole(UserRole role) {
        this.role = role;

        switch (role) {
            case ADMIN -> this.roleName = new AdminRole();
            case TEACHER -> this.roleName = new TeacherRole();
        }
    }
}
