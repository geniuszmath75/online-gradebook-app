package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST IT: grade CRUD plus business rule (one grade per student-test pair).
 */
public class GradeCrudBusinessRuleIntegrationTest extends RestIntegrationTestSupport {

    @Test
    void shouldSupportGradeCrudAndRejectDuplicateStudentTestGrade() throws Exception {
        String adminToken = ensureAdminToken();

        String teacherEmail = uniqueEmail("teacher");
        registerUser(teacherEmail, "teacher123", "Anna", "Nowak", UserRole.TEACHER)
                .andExpect(status().isCreated());
        String teacherToken = login(teacherEmail, "teacher123");
        Long teacherId = userRepository.findByEmail(teacherEmail).orElseThrow().getId();

        String studentEmail = uniqueEmail("student");
        registerStudent(studentEmail, "student123", "Piotr", "Zielinski")
                .andExpect(status().isCreated());
        Long studentId = studentRepository.findByEmail(studentEmail).orElseThrow().getId();

        String className = "IT-" + System.nanoTime();
        String subjectName = "Sub-" + System.nanoTime();
        createClass(adminToken, className);
        createSubject(adminToken, subjectName);
        Long classId = classRepository.findByName(className).orElseThrow().getId();
        Long subjectId = subjectRepository.findByName(subjectName).orElseThrow().getId();

        SubjectDto subjectDto = new SubjectDto();
        subjectDto.setName(subjectName);
        updateUserSubjects(teacherToken, List.of(subjectDto), teacherId);

        String testName = "Test-" + System.nanoTime();
        createTest(teacherToken, testName, TestCategory.CLASS_TEST, LocalDate.now().plusDays(7), classId, subjectId);
        Long testId = testRepository.findByName(testName).orElseThrow().getId();

        createGrade(teacherToken, BigDecimal.valueOf(4.5), "Good", studentId, testId);
        Long gradeId = gradeRepository.findGradeByStudentIdAndTestId(studentId, testId).orElseThrow().getId();

        // Business rule: one grade per student + test
        createGradeRaw(teacherToken, BigDecimal.valueOf(5.0), "Duplicate", studentId, testId)
                .andExpect(status().isConflict());

        GradeUpdateDto updateDto = new GradeUpdateDto();
        updateDto.setGrade(BigDecimal.valueOf(5.0));
        updateDto.setDescription("Updated");
        patchWithToken("/api/grades/" + gradeId, teacherToken, updateDto)
                .andExpect(status().isOk());

        getWithToken("/api/grades/" + gradeId, teacherToken)
                .andExpect(status().isOk());

        deleteWithToken("/api/grades/" + gradeId, teacherToken)
                .andExpect(status().isOk());

        getWithToken("/api/grades/" + gradeId, teacherToken)
                .andExpect(status().isBadRequest());
    }
}
