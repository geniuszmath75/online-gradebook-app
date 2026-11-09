package org.example.onlinegradebookapp.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

class CorsConfigTest {
    @Test
    void shouldAllowConfiguredFrontendOriginsAndApiMethods() {
        CorsConfigurationSource source = new CorsConfig(
                "http://localhost:5173, https://gradebook.example.com"
        ).corsConfigurationSource();

        CorsConfiguration configuration = source.getCorsConfiguration(apiRequest());

        assertThat(configuration).isNotNull();
        assertThat(configuration.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
        assertThat(configuration.checkOrigin("https://gradebook.example.com"))
                .isEqualTo("https://gradebook.example.com");
        assertThat(configuration.checkOrigin("https://untrusted.example.com")).isNull();
        assertThat(configuration.getAllowedMethods())
                .containsExactly("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        assertThat(configuration.getAllowedHeaders()).containsExactly("Authorization", "Content-Type");
        assertThat(configuration.getAllowCredentials()).isFalse();
    }

    @Test
    void shouldNotApplyToNonApiPaths() {
        CorsConfigurationSource source = new CorsConfig("http://localhost:5173").corsConfigurationSource();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/swagger-ui/index.html");

        assertThat(source.getCorsConfiguration(request)).isNull();
    }

    private MockHttpServletRequest apiRequest() {
        return new MockHttpServletRequest("GET", "/api/grades");
    }
}
