package org.example.onlinegradebookapp.payload.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class GradeResponse {
    private Long id;
    private BigDecimal grade;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private Long studentId;
    private Long testId;
}
