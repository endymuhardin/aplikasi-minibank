package id.ac.tazkia.minibank.functional.success;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import id.ac.tazkia.minibank.functional.config.BasePlaywrightTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Functional test for Actuator health check endpoint.
 * Verifies that the health endpoint is accessible and returns correct status.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Actuator Health Check - Success Scenarios")
class ActuatorHealthCheckTest extends BasePlaywrightTest {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private APIRequestContext request;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
    }

    @AfterAll
    static void afterAll() {
        if (playwright != null) {
            playwright.close();
        }
    }

    private APIRequestContext getRequestContext() {
        if (request == null) {
            request = playwright.request().newContext(new APIRequest.NewContextOptions()
                    .setBaseURL("http://localhost:" + port));
        }
        return request;
    }

    @Test
    @DisplayName("Health endpoint should return UP status")
    void healthEndpointShouldReturnUpStatus() {
        // When
        APIResponse response = getRequestContext().get("/actuator/health");

        // Then
        assertThat(response.ok()).isTrue();
        assertThat(response.status()).isEqualTo(200);

        String body = response.text();
        assertThat(body).contains("\"status\":\"UP\"");
    }

    @Test
    @DisplayName("Health endpoint should be accessible without authentication")
    void healthEndpointShouldBeAccessibleWithoutAuthentication() {
        // When - no authentication headers provided
        APIResponse response = getRequestContext().get("/actuator/health");

        // Then
        assertThat(response.ok()).isTrue();
        assertThat(response.status()).isEqualTo(200);
    }

    @Test
    @DisplayName("Health endpoint should return JSON content type")
    void healthEndpointShouldReturnJsonContentType() {
        // When
        APIResponse response = getRequestContext().get("/actuator/health");

        // Then
        assertThat(response.ok()).isTrue();
        String contentType = response.headers().get("content-type");
        assertThat(contentType).containsAnyOf("application/json", "application/vnd.spring-boot.actuator.v3+json");
    }

    @Test
    @DisplayName("Info endpoint should be accessible without authentication")
    void infoEndpointShouldBeAccessibleWithoutAuthentication() {
        // When
        APIResponse response = getRequestContext().get("/actuator/info");

        // Then
        assertThat(response.ok()).isTrue();
        assertThat(response.status()).isEqualTo(200);
    }
}
