package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.entity.User;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.payload.request.GradeUpdateDto;
import org.example.onlinegradebookapp.payload.request.KnowledgeTestUpdateDto;
import org.example.onlinegradebookapp.payload.request.SubjectDto;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FullApplicationFlowIntegrationTest extends RestIntegrationTestSupport {

    private String adminToken;
    private String teacher1Token;
    private String teacher2Token;
    private String student1Token;
    private String student2Token;

    private Long class1AId, class1BId;
    private Long mathId, historyId, csId;
    private Long student1Id, student2Id;
    private Long teacher1Id, teacher2Id;
    private Long test1Id, test2Id;
    private Long grade1Id, grade2Id;

    @BeforeAll
    void setUp() throws Exception {
        registerUser("admin@gmail.com", "admin123", "Admin", "admin", UserRole.ADMIN)
                .andExpect(status().isCreated());
        adminToken = login("admin@gmail.com", "admin123");
    }

    @Test
    void shouldCorrectlyHandleFullApplicationFlowAndAccessControl() throws Exception {
        // 1. Attempting to create a second ADMIN (should fail)
        UserRegistrationDto duplicateAdmin = new UserRegistrationDto();
        duplicateAdmin.setEmail("admin2@gmail.com");
        duplicateAdmin.setPassword("admin321");
        duplicateAdmin.setFirstName("Admin");
        duplicateAdmin.setLastName("duplicate");
        duplicateAdmin.setRole(UserRole.ADMIN);

        postWithToken("/api/auth/register/user", adminToken, duplicateAdmin)
                .andExpect(status().isConflict());

        // 2. TEACHER registration
        registerUser("teacher1@gmail.com", "teacher123", "Jan", "Kowalski", UserRole.TEACHER)
                .andExpect(status().isCreated());
        // Login teacher1
        teacher1Token = login("teacher1@gmail.com", "teacher123");

        // Get teacher1 ID
        teacher1Id = userRepository.findByEmail("teacher1@gmail.com").orElseThrow().getId();

        registerUser("teacher2@gmail.com", "teacher321", "Adam", "Nowak", UserRole.TEACHER)
                .andExpect(status().isCreated());
        // Login teacher2
        teacher2Token = login("teacher2@gmail.com", "teacher321");

        // Get teacher2 ID
        teacher2Id = userRepository.findByEmail("teacher2@gmail.com").orElseThrow().getId();

        // 3. STUDENT registration
        registerStudent("student1@gmail.com", "student123", "Dawid", "Kowal")
                .andExpect(status().isCreated());
        // Login student1
        student1Token = login("student1@gmail.com", "student123");

        // Get student1 ID
        student1Id = studentRepository.findByEmail("student1@gmail.com").orElseThrow().getId();

        registerStudent("student2@gmail.com", "student321", "Anna", "Kwiatkowska")
                .andExpect(status().isCreated());
        // Login student2
        student2Token = login("student2@gmail.com", "student321");

        // Get student2 ID
        student2Id = studentRepository.findByEmail("student2@gmail.com").orElseThrow().getId();

        // 4. Creating school classes
        createClass(adminToken, "1A");
        // Get class1A ID
        class1AId = classRepository.findByName("1A").orElseThrow().getId();

        createClass(adminToken, "1B");
        // Get class1B ID
        class1BId = classRepository.findByName("1B").orElseThrow().getId();

        // 5. Creating subjects
        createSubject(adminToken, "matematyka");

        // Get math ID
        mathId = subjectRepository.findByName("matematyka").orElseThrow().getId();

        createSubject(adminToken, "historia");
        // Get history ID
        historyId = subjectRepository.findByName("historia").orElseThrow().getId();

        createSubject(adminToken, "informatyka");

        // Get computer science ID
        csId = subjectRepository.findByName("informatyka").orElseThrow().getId();

        // 6. TEACHERS' update (classId assignment)
        // Login teacher1
        updateUserClass(teacher1Token, class1AId, teacher1Id);

        // Login teacher2
        updateUserClass(teacher2Token, class1BId, teacher2Id);

        // 7. TEACHERS' update – subjects assigment
        SubjectDto math = new SubjectDto();
        math.setName("matematyka");
        SubjectDto history = new SubjectDto();
        history.setName("historia");
        SubjectDto cs = new SubjectDto();
        cs.setName("informatyka");
        List<SubjectDto> teacher1Subjects = List.of(math, cs);
        List<SubjectDto> teacher2Subjects = List.of(history);

        updateUserSubjects(teacher1Token, teacher1Subjects, teacher1Id);
        updateUserSubjects(teacher2Token, teacher2Subjects, teacher2Id);

        // 9. Teacher1 create test
        createTest(teacher1Token, "Trygonometria", TestCategory.CLASS_TEST, LocalDate.now().plusDays(10), class1AId, mathId);
        // Get math test ID
        test1Id = testRepository.findByName("Trygonometria").orElseThrow().getId();

        // 10. Teacher1 create grade for student1
        createGrade(teacher1Token, BigDecimal.valueOf(5.0), "Dobra robota", student1Id, test1Id);
        grade1Id = gradeRepository.findGradeByStudentIdAndTestId(student1Id, test1Id).orElseThrow().getId();

        // 11. Teacher2 create test
        createTest(teacher2Token, "II wojna światowa", TestCategory.QUIZ, LocalDate.now().plusDays(5), class1BId, historyId);
        test2Id = testRepository.findByName("II wojna światowa").orElseThrow().getId();

        // 12. Teacher2 is trying to edit Teacher1 test
        KnowledgeTestUpdateDto updateTest = new KnowledgeTestUpdateDto();
        updateTest.setCategory(TestCategory.HOMEWORK);
        patchWithToken("/api/knowledge_tests/" + test1Id, teacher2Token, updateTest)
                .andExpect(status().isUnauthorized());

        // 13. Teacher2 create grade for student2
        createGrade(teacher2Token, BigDecimal.valueOf(4.5), "Ok", student2Id, test2Id);
        grade2Id = gradeRepository.findGradeByStudentIdAndTestId(student2Id, test2Id).orElseThrow().getId();

        // 14. Teacher2 is trying to edit Teacher1 grade
        GradeUpdateDto gradeUpdate = new GradeUpdateDto();
        gradeUpdate.setGrade(BigDecimal.valueOf(3.0));
        patchWithToken("/api/grades/" + grade1Id, teacher2Token, gradeUpdate)
                .andExpect(status().isUnauthorized());

        // 15-17. Student1 i Student2 - checking access
        getWithToken("/api/students/" + student1Id, student1Token).andExpect(status().isOk());
        getWithToken("/api/students", student1Token).andExpect(status().isForbidden());
        getWithToken("/api/students/" + student2Id, student2Token).andExpect(status().isOk());
        deleteWithToken("/api/students/" + student1Id, student2Token).andExpect(status().isForbidden());

        // 18. Teacher1 fetches list of students
        getWithToken("/api/students", teacher1Token).andExpect(status().isOk());

        // 19. Teacher1 removes its grade and test
        deleteWithToken("/api/grades/" + grade1Id, teacher1Token).andExpect(status().isOk());
        deleteWithToken("/api/knowledge_tests/" + test1Id, teacher1Token).andExpect(status().isOk());

        // 20-21. Teacher2 fetches data, tries to delete the Teacher1
        getWithToken("/api/users/" + teacher2Id, teacher2Token).andExpect(status().isOk());
        deleteWithToken("/api/users/" + teacher1Id, teacher2Token).andExpect(status().isForbidden());

        // 22-25. Admin cleanup
        getWithToken("/api/users", adminToken).andExpect(status().isOk());
        deleteWithToken("/api/grades/" + grade2Id, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/knowledge_tests/" + test2Id, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/students/" + student1Id, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/students/" + student2Id, adminToken).andExpect(status().isOk());

        // Ensure that school class detached
        User teacher1 = userRepository.findById(teacher1Id).orElseThrow();
        teacher1.setSchoolClass(null);
        userRepository.save(teacher1);

        deleteWithToken("/api/users/" + teacher1Id, adminToken).andExpect(status().isOk());

        // Ensure that school class detached
        User teacher2 = userRepository.findById(teacher2Id).orElseThrow();
        teacher2.setSchoolClass(null);
        userRepository.save(teacher2);

        deleteWithToken("/api/users/" + teacher2Id, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/classes/" + class1AId, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/subjects/" + mathId, adminToken).andExpect(status().isOk());
        deleteWithToken("/api/subjects/" + historyId, adminToken).andExpect(status().isOk());
    }
}
