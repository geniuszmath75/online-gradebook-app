package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;

import java.time.LocalDate;

@Data
public class KnowledgeTestDto {
    @NotBlank(message = "Test name cannot be empty")
    private String name;

    @NotNull(message = "Category cannot be empty")
    private TestCategory category;

    @Future(message = "Test date must be in the future ")
    @NotNull(message = "Test date cannot be empty")
    private LocalDate testDate;

    @NotNull(message = "Class ID cannot be empty")
    private Long classId;

    @NotNull(message = "Subject ID cannot be empty")
    private Long subjectId;

    @NotNull(message = "Teacher ID cannot be empty")
    private Long teacherId;
}
