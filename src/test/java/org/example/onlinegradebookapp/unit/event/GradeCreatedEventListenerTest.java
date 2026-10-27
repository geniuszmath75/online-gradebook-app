package org.example.onlinegradebookapp.unit.event;

import org.example.onlinegradebookapp.event.GradeCreatedEvent;
import org.example.onlinegradebookapp.event.GradeCreatedEventListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class GradeCreatedEventListenerTest {

    @Test
    void onGradeCreated_shouldWriteAuditLog(CapturedOutput output) {
        GradeCreatedEventListener listener = new GradeCreatedEventListener();
        GradeCreatedEvent event = new GradeCreatedEvent(
                10L, 1L, 2L, BigDecimal.valueOf(4.5), 5L
        );

        listener.onGradeCreated(event);

        assertThat(output.getOut())
                .contains("AUDIT grade created")
                .contains("gradeId=10")
                .contains("studentId=1")
                .contains("testId=2")
                .contains("value=4.5")
                .contains("createdByUserId=5");
    }
}
