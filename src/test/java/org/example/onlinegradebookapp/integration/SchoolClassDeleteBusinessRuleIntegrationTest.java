package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.payload.request.StudentRegistrationDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST IT: classes with related resources cannot be deleted.
 */
public class SchoolClassDeleteBusinessRuleIntegrationTest extends RestIntegrationTestSupport {

    @Test
    void shouldRejectDeletingClassWithRelatedResources() throws Exception {
        String adminToken = ensureAdminToken();

        String className = "Delete-Conflict-" + System.nanoTime();
        String subjectName = "Delete-Subject-" + System.nanoTime();
        createClass(adminToken, className);
        createSubject(adminToken, subjectName);
        Long classId = classRepository.findByName(className).orElseThrow().getId();
        Long subjectId = subjectRepository.findByName(subjectName).orElseThrow().getId();

        String teacherEmail = uniqueEmail("class-teacher");
        UserRegistrationDto teacherDto = new UserRegistrationDto();
        teacherDto.setEmail(teacherEmail);
        teacherDto.setPassword("teacher123");
        teacherDto.setFirstName("Class");
        teacherDto.setLastName("Teacher");
        teacherDto.setRole(UserRole.TEACHER);
        teacherDto.setClassId(classId);
        postWithToken("/api/auth/register/user", adminToken, teacherDto)
                .andExpect(status().isCreated());
        String teacherToken = login(teacherEmail, "teacher123");
        Long teacherId = userRepository.findByEmail(teacherEmail).orElseThrow().getId();

        StudentRegistrationDto studentDto = new StudentRegistrationDto();
        studentDto.setEmail(uniqueEmail("class-student"));
        studentDto.setPassword("student123");
        studentDto.setFirstName("Class");
        studentDto.setLastName("Student");
        studentDto.setClassId(classId);
        postWithToken("/api/auth/register/student", adminToken, studentDto)
                .andExpect(status().isCreated());
        Long studentId = studentRepository.findByEmail(studentDto.getEmail()).orElseThrow().getId();

        String testName = "Delete-Test-" + System.nanoTime();
        createTest(
                teacherToken,
                testName,
                TestCategory.CLASS_TEST,
                LocalDate.now().plusDays(7),
                classId,
                subjectId
        );
        Long testId = testRepository.findByName(testName).orElseThrow().getId();
        createGrade(teacherToken, BigDecimal.valueOf(4.5), "Protected", studentId, testId);
        Long gradeId = gradeRepository.findGradeByStudentIdAndTestId(studentId, testId)
                .orElseThrow()
                .getId();

        deleteWithToken("/api/classes/" + classId, adminToken)
                .andExpect(status().isConflict());

        getWithToken("/api/classes/" + classId, adminToken)
                .andExpect(status().isOk());
        getWithToken("/api/grades/" + gradeId, teacherToken)
                .andExpect(status().isOk());

        deleteWithToken("/api/grades/" + gradeId, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/knowledge_tests/" + testId, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/students/" + studentId, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/users/" + teacherId, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/classes/" + classId, adminToken).andExpect(status().isOk());
        deleteWithToken(
                "/api/subjects/" + subjectId,
                adminToken
        ).andExpect(status().isOk());
    }
}
