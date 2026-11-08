package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.example.onlinegradebookapp.payload.request.UserRegistrationDto;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST IT: registration + login success/failure.
 */
public class AuthFlowIntegrationTest extends RestIntegrationTestSupport {

    @Test
    void shouldRegisterTeacherAndLoginWithValidCredentials() throws Exception {
        String email = uniqueEmail("teacher");
        String password = "teacher123";

        registerUser(email, password, "Jan", "Kowalski", UserRole.TEACHER)
                .andExpect(status().isCreated());

        loginRaw(email, password)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(email));

        loginRaw(email, "wrong-password")
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowOnlyAdminToRegisterUsers() throws Exception {
        String adminToken = ensureAdminToken();
        String teacherEmail = uniqueEmail("existing-teacher");
        registerUser(
                teacherEmail,
                "teacher123",
                "Existing",
                "Teacher",
                UserRole.TEACHER
        ).andExpect(status().isCreated());
        String teacherToken = login(teacherEmail, "teacher123");

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail(uniqueEmail("blocked"));
        dto.setPassword("blocked123");
        dto.setFirstName("Blocked");
        dto.setLastName("User");
        dto.setRole(UserRole.TEACHER);

        postWithToken("/api/auth/register/user", teacherToken, dto)
                .andExpect(status().isForbidden());

        dto.setEmail(uniqueEmail("second-admin"));
        dto.setRole(UserRole.ADMIN);
        postWithToken("/api/auth/register/user", adminToken, dto)
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/register/user")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectRegisteringSecondTeacherForSameClass() throws Exception {
        String adminToken = ensureAdminToken();
        String className = "Teacher-Conflict-" + System.nanoTime();
        createClass(adminToken, className);
        Long classId = classRepository.findByName(className).orElseThrow().getId();

        UserRegistrationDto first = new UserRegistrationDto();
        first.setEmail(uniqueEmail("first-class-teacher"));
        first.setPassword("teacher123");
        first.setFirstName("First");
        first.setLastName("Teacher");
        first.setRole(UserRole.TEACHER);
        first.setClassId(classId);
        postWithToken("/api/auth/register/user", adminToken, first)
                .andExpect(status().isCreated());

        UserRegistrationDto second = new UserRegistrationDto();
        second.setEmail(uniqueEmail("second-class-teacher"));
        second.setPassword("teacher123");
        second.setFirstName("Second");
        second.setLastName("Teacher");
        second.setRole(UserRole.TEACHER);
        second.setClassId(classId);
        postWithToken("/api/auth/register/user", adminToken, second)
                .andExpect(status().isConflict());

        Long firstTeacherId = userRepository.findByEmail(first.getEmail()).orElseThrow().getId();
        deleteWithToken("/api/users/" + firstTeacherId, adminToken)
                .andExpect(status().isOk());
        deleteWithToken("/api/classes/" + classId, adminToken)
                .andExpect(status().isOk());
    }
}
