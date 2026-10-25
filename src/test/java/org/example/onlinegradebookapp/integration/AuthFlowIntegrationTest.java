package org.example.onlinegradebookapp.integration;

import org.example.onlinegradebookapp.entity.UserRole.UserRole;
import org.junit.jupiter.api.Test;

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
}
