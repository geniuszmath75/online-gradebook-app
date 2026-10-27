package org.example.onlinegradebookapp.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Asynchronously audits newly created grades
 */
@Component
public class GradeCreatedEventListener {
    private static final Logger log = LoggerFactory.getLogger(GradeCreatedEventListener.class);

    @Async
    @EventListener
    public void onGradeCreated(GradeCreatedEvent event) {
        log.info(
                "AUDIT grade created: gradeId={}, studentId={}, testId={}, value={}, createdByUserId={}",
                event.gradeId(),
                event.studentId(),
                event.testId(),
                event.gradeValue(),
                event.createdByUserId()
        );
    }
}
