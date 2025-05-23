package org.example.onlinegradebookapp.payload.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GradeDto {
    @NotNull(message = "Grade value cannot be empty")
    @Min(message = "The lowest grade value is 1.0", value = 1)
    @Max(message = "The highest grade value is 6.0", value = 6)
    private BigDecimal grade;

    private String description;

    private Long studentId;

    private Long testId;
}
