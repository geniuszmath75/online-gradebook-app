package org.example.onlinegradebookapp.event;

import java.math.BigDecimal;

/**
 * Domain event published after a grade is successfully persisted.
 */
public record GradeCreatedEvent(
        Long gradeId,
        Long studentId,
        Long testId,
        BigDecimal gradeValue,
        Long createdByUserId
) {
}
