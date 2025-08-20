package id.ac.tazkia.minibank.config;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to check if the Spring Boot application is ready to serve requests.
 * Replaces Thread.sleep with proper HTTP-based readiness checks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationReadinessService {

    private final SeleniumTestProperties properties;

    /**
     * Wait for the Spring Boot application to be ready to serve requests.
     * Uses HTTP health checks instead of arbitrary sleep delays.
     *
     * @param webappPort the port where the application is running
     * @return true if application is ready, false if timeout
     */
    public boolean waitForApplicationReady(int webappPort) {
        if (!properties.getApplicationReadiness().isEnabled()) {
            log.info("🔄 APPLICATION READINESS: Checks disabled, assuming application is ready");
            return true;
        }

        String threadName = Thread.currentThread().getName();
        String healthCheckUrl = "http://localhost:" + webappPort + "/login";
        
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.getApplicationReadiness().getHttpTimeoutSeconds()))
                .build();

        int maxAttempts = properties.getApplicationReadiness().getMaxWaitTimeSeconds() * 1000 
                         / properties.getApplicationReadiness().getPollIntervalMs();
        
        log.info("⏳ WAITING FOR APPLICATION: Checking readiness on localhost:{} [Thread: {}, MaxAttempts: {}]", 
                webappPort, threadName, maxAttempts);

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(healthCheckUrl))
                        .timeout(Duration.ofSeconds(properties.getApplicationReadiness().getHttpTimeoutSeconds()))
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    log.info("✅ APPLICATION READY: App ready on localhost:{} after {} attempts [Thread: {}]", 
                            webappPort, attempt + 1, threadName);
                    return true;
                }

            } catch (Exception e) {
                // Expected during startup, continue waiting
                if (attempt % 10 == 0) { // Log every 10 attempts to reduce noise
                    log.debug("App not ready yet on attempt {} [Thread: {}]: {}", 
                             attempt + 1, threadName, e.getMessage());
                }
            }

            try {
                Thread.sleep(properties.getApplicationReadiness().getPollIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Application readiness check interrupted [Thread: {}]", threadName);
                return false;
            }
        }

        log.error("❌ APPLICATION NOT READY: App failed to start within {} seconds on port {} [Thread: {}]", 
                 properties.getApplicationReadiness().getMaxWaitTimeSeconds(), webappPort, threadName);
        return false;
    }

    /**
     * Asynchronously wait for application readiness.
     * Useful for non-blocking readiness checks.
     */
    public CompletableFuture<Boolean> waitForApplicationReadyAsync(int webappPort) {
        return CompletableFuture.supplyAsync(() -> waitForApplicationReady(webappPort));
    }

    /**
     * Check if application is ready with a single HTTP call (no polling).
     */
    public boolean isApplicationReady(int webappPort) {
        if (!properties.getApplicationReadiness().isEnabled()) {
            return true;
        }

        try {
            String healthCheckUrl = "http://localhost:" + webappPort + "/login";
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(2))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(healthCheckUrl))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }
}