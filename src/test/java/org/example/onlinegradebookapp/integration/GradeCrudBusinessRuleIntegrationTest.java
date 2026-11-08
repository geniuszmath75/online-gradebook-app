package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

        String otherTeacherEmail = uniqueEmail("other-teacher");
        registerUser(otherTeacherEmail, "teacher456", "Other", "Teacher", UserRole.TEACHER)
                .andExpect(status().isCreated());
        String otherTeacherToken = login(otherTeacherEmail, "teacher456");

        String otherTestName = "Other-Test-" + System.nanoTime();
        createTest(otherTeacherToken, otherTestName, TestCategory.QUIZ,
                LocalDate.now().plusDays(8), classId, subjectId);
        Long otherTestId = testRepository.findByName(otherTestName).orElseThrow().getId();
        createGrade(otherTeacherToken, BigDecimal.valueOf(3.5), "Other", studentId, otherTestId);
        Long otherGradeId = gradeRepository.findGradeByStudentIdAndTestId(studentId, otherTestId)
                .orElseThrow()
                .getId();

        getWithToken("/api/grades", teacherToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
        getWithToken("/api/grades", otherTeacherToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
        getWithToken("/api/knowledge_tests", teacherToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        getWithToken("/api/knowledge_tests", otherTeacherToken)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        getWithToken("/api/grades/" + otherGradeId, teacherToken)
                .andExpect(status().isForbidden());
        deleteWithToken("/api/grades/" + otherGradeId, teacherToken)
                .andExpect(status().isForbidden());
        getWithToken("/api/grades/" + otherGradeId, otherTeacherToken)
                .andExpect(status().isOk());

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
                .andExpect(status().isNotFound());
    }
}
