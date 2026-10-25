package org.example.onlinegradebookapp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.onlinegradebookapp.entity.TestCategory.TestCategory;
import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.payload.request.*;
import org.example.onlinegradebookapp.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class RestIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected StudentRepository studentRepository;

    @Autowired
    protected SchoolClassRepository classRepository;

    @Autowired
    protected SubjectRepository subjectRepository;

    @Autowired
    protected KnowledgeTestRepository testRepository;

    @Autowired
    protected GradeRepository gradeRepository;

    protected static final String SHARED_ADMIN_EMAIL = "it-shared-admin@example.com";
    protected static final String SHARED_ADMIN_PASSWORD = "admin123";

    protected String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    /**
     * Reuses an existing ADMIN (at most one is allowed) or creates a shared IT admin.
     */
    protected String ensureAdminToken() throws Exception {
        if (userRepository.countByRole(UserRole.ADMIN) == 0) {
            registerUser(SHARED_ADMIN_EMAIL, SHARED_ADMIN_PASSWORD, "IT", "Admin", UserRole.ADMIN)
                    .andExpect(status().isCreated());
            return login(SHARED_ADMIN_EMAIL, SHARED_ADMIN_PASSWORD);
        }
        if (Boolean.TRUE.equals(userRepository.existsByEmail(SHARED_ADMIN_EMAIL))) {
            return login(SHARED_ADMIN_EMAIL, SHARED_ADMIN_PASSWORD);
        }
        // Compatible with FullApplicationFlowIntegrationTest seed admin
        if (Boolean.TRUE.equals(userRepository.existsByEmail("admin@gmail.com"))) {
            return login("admin@gmail.com", "admin123");
        }
        throw new IllegalStateException("An ADMIN user exists but login credentials are unknown");
    }

    protected String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    protected ResultActions loginRaw(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\", \"password\": \"" + password + "\"}"));
    }

    protected ResultActions postWithToken(String url, String token, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions patchWithToken(String url, String token, Object body) throws Exception {
        return mockMvc.perform(patch(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    protected ResultActions getWithToken(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    protected ResultActions deleteWithToken(String url, String token) throws Exception {
        return mockMvc.perform(delete(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    protected ResultActions registerUser(String email, String password, String firstName, String lastName, UserRole role) throws Exception {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setRole(role);

        return mockMvc.perform(post("/api/auth/register/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    protected ResultActions registerStudent(String email, String password, String firstName, String lastName) throws Exception {
        StudentRegistrationDto dto = new StudentRegistrationDto();
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);

        return mockMvc.perform(post("/api/auth/register/student")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    protected void createClass(String adminToken, String name) throws Exception {
        postWithToken("/api/classes", adminToken, Map.of("name", name))
                .andExpect(status().isCreated());
    }

    protected void createSubject(String adminToken, String name) throws Exception {
        postWithToken("/api/subjects", adminToken, Map.of("name", name))
                .andExpect(status().isCreated());
    }

    protected void createTest(String token, String name, TestCategory category, LocalDate testDate,
                              Long classId, Long subjectId) throws Exception {
        KnowledgeTestDto dto = new KnowledgeTestDto();
        dto.setName(name);
        dto.setCategory(category);
        dto.setTestDate(testDate);
        dto.setClassId(classId);
        dto.setSubjectId(subjectId);

        postWithToken("/api/knowledge_tests", token, dto)
                .andExpect(status().isCreated());
    }

    protected void createGrade(String token, BigDecimal gradeValue, String description,
                               Long studentId, Long testId) throws Exception {
        GradeDto dto = new GradeDto();
        dto.setGrade(gradeValue);
        dto.setDescription(description);
        dto.setStudentId(studentId);
        dto.setTestId(testId);

        postWithToken("/api/grades", token, dto)
                .andExpect(status().isCreated());
    }

    protected ResultActions createGradeRaw(String token, BigDecimal gradeValue, String description,
                                           Long studentId, Long testId) throws Exception {
        GradeDto dto = new GradeDto();
        dto.setGrade(gradeValue);
        dto.setDescription(description);
        dto.setStudentId(studentId);
        dto.setTestId(testId);

        return postWithToken("/api/grades", token, dto);
    }

    protected void updateUserClass(String token, Long classId, Long userId) throws Exception {
        patchWithToken("/api/users/" + userId, token, Map.of("classId", classId))
                .andExpect(status().isOk());
    }

    protected void updateUserSubjects(String token, List<SubjectDto> subjects, Long userId) throws Exception {
        patchWithToken("/api/users/" + userId, token, Map.of("subjects", subjects))
                .andExpect(status().isOk());
    }
}

