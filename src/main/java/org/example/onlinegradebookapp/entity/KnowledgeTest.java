package org.example.onlinegradebookapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.example.onlinegradebookapp.entity.TestCategory.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "knowledge_tests")
public class KnowledgeTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestCategory category;

    @Transient
    private CategoryName categoryName;

    @Column(name = "test_date", nullable = false)
    @Future
    private LocalDate testDate;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    public void setCategoryName(TestCategory category) {
        this.category = category;

        switch (category) {
            case QUIZ -> this.categoryName = new QuizCategory();
            case CLASS_TEST -> this.categoryName = new ClassTestCategory();
            case HOMEWORK -> this.categoryName = new HomeworkCategory();
            case ORAL_ANSWER -> this.categoryName = new OralAnswerCategory();
            case CLASSWORK -> this.categoryName = new ClassworkCategory();
            case OTHER -> this.categoryName = new OtherCategory();
        }
    }
}
