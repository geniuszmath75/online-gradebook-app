package org.example.onlinegradebookapp.payload;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;

import java.time.LocalDate;

@Data
public class KnowledgeTestDto {
    @NotBlank(message = "Test name cannot be empty")
    private String name;

    private TestCategory category;

    @Future(message = "Test date must be in the future ")
    private LocalDate testDate;

    private Long classId;

    private Long subjectId;

    private Long teacherId;
}
