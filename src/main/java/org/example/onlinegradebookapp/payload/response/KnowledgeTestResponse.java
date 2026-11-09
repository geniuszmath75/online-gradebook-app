package org.example.onlinegradebookapp.payload.response;

import lombok.Data;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class KnowledgeTestResponse {
    private Long id;
    private String name;
    private TestCategory category;
    private LocalDate testDate;
    private Instant createdAt;
    private Instant updatedAt;
    private Long classId;
    private Long subjectId;
}
