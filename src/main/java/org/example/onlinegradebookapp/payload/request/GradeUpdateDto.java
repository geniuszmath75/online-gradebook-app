package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.example.onlinegradebookapp.validation.AtLeastOneField;

import java.math.BigDecimal;

@Data
@AtLeastOneField(fields = {"grade", "description", "studentId", "testId"})
public class GradeUpdateDto {
    @Min(message = "The lowest grade value is 1.0", value = 1)
    @Max(message = "The highest grade value is 6.0", value = 6)
    private BigDecimal grade;

    private String description;

    private Long studentId;

    private Long testId;
}
