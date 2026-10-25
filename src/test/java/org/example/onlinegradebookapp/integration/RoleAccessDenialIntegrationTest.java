package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REST IT: wrong role is denied access to protected endpoints.
 */
public class RoleAccessDenialIntegrationTest extends RestIntegrationTestSupport {

    @Test
    void shouldDenyStudentAccessToTeacherOnlyEndpoints() throws Exception {
        String adminToken = ensureAdminToken();

        String teacherEmail = uniqueEmail("teacher");
        registerUser(teacherEmail, "teacher123", "Ewa", "Lewandowska", UserRole.TEACHER)
                .andExpect(status().isCreated());
        Long teacherId = userRepository.findByEmail(teacherEmail).orElseThrow().getId();

        String studentEmail = uniqueEmail("student");
        registerStudent(studentEmail, "student123", "Kasia", "Wojcik")
                .andExpect(status().isCreated());
        String studentToken = login(studentEmail, "student123");
        Long studentId = studentRepository.findByEmail(studentEmail).orElseThrow().getId();

        // Student may read own profile, but not list all students (@PreAuthorize ADMIN/TEACHER)
        getWithToken("/api/students/" + studentId, studentToken)
                .andExpect(status().isOk());
        getWithToken("/api/students", studentToken)
                .andExpect(status().isForbidden());

        // Student cannot access grades API (role filter on SecurityConfig)
        getWithToken("/api/grades", studentToken)
                .andExpect(status().isForbidden());

        // Teacher cannot delete another user (ADMIN only)
        String teacherToken = login(teacherEmail, "teacher123");
        deleteWithToken("/api/users/" + teacherId, teacherToken)
                .andExpect(status().isForbidden());

        // Admin still can manage users
        getWithToken("/api/users", adminToken)
                .andExpect(status().isOk());
    }
}
