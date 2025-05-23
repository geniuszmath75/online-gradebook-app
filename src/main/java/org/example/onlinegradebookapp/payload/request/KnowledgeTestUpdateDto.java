package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.Future;
import lombok.Data;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.validation.AtLeastOneField;

import java.time.LocalDate;

@Data
@AtLeastOneField(fields = {"name", "category", "testDate", "classId", "subjectId", "teacherId"})
public class KnowledgeTestUpdateDto {
    private String name;

    private TestCategory category;

    @Future(message = "Test date must be in the future ")
    private LocalDate testDate;

    private Long classId;

    private Long subjectId;

    private Long teacherId;
}
