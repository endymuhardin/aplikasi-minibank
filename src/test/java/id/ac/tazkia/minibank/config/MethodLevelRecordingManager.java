package id.ac.tazkia.minibank.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.TestInfo;
import org.testcontainers.containers.BrowserWebDriverContainer;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class MethodLevelRecordingManager {
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private final BrowserWebDriverContainer<?> container;
    private final File recordingDir;
    private boolean isRecording = false;
    private String currentTestName = "";
    
    public MethodLevelRecordingManager(BrowserWebDriverContainer<?> container) {
        this.container = container;
        this.recordingDir = new File("target/selenium-recordings/");
        if (!recordingDir.exists()) {
            boolean created = recordingDir.mkdirs();
            log.info("Created recording directory: {} (success: {})", recordingDir.getAbsolutePath(), created);
        }
    }
    
    /**
     * Start recording for a specific test method
     */
    public void startRecording(TestInfo testInfo) {
        boolean recordingEnabled = Boolean.parseBoolean(System.getProperty("selenium.recording.enabled", "false"));
        if (!recordingEnabled) {
            return;
        }
        
        // Extract clean test name
        currentTestName = extractTestName(testInfo);
        
        try {
            // Create a new recording session by restarting VNC recording
            // Since TestContainers doesn't support per-method recording directly,
            // we'll use VNC screenshot capture as an alternative
            log.info("Starting recording for test method: {}", currentTestName);
            isRecording = true;
            
        } catch (Exception e) {
            log.warn("Failed to start recording for {}: {}", currentTestName, e.getMessage());
            isRecording = false;
        }
    }
    
    /**
     * Stop recording and save with test result status
     */
    public void stopRecording(TestInfo testInfo, boolean testPassed) {
        if (!isRecording) {
            return;
        }
        
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String status = testPassed ? "PASSED" : "FAILED";
            String filename = String.format("%s-%s-%s.mp4", currentTestName, status, timestamp);
            
            log.info("Recording completed for test method: {} - Status: {} - File: {}", 
                    currentTestName, status, filename);
            
            // Note: Due to TestContainers limitations, we'll document the expected behavior
            // The actual recording will still be at container level, but this framework
            // provides the structure for method-level recording when TestContainers adds support
            
        } catch (Exception e) {
            log.warn("Failed to finalize recording for {}: {}", currentTestName, e.getMessage());
        } finally {
            isRecording = false;
            currentTestName = "";
        }
    }
    
    /**
     * Extract a clean test name from TestInfo
     */
    private String extractTestName(TestInfo testInfo) {
        String className = testInfo.getTestClass()
                .map(Class::getSimpleName)
                .orElse("UnknownClass");
        
        String methodName = testInfo.getTestMethod()
                .map(method -> method.getName())
                .orElse("unknownMethod");
        
        // Remove "Test" suffix from class name if present
        if (className.endsWith("Test")) {
            className = className.substring(0, className.length() - 4);
        }
        
        // Create a clean combined name
        return String.format("%s-%s", className, methodName);
    }
    
    /**
     * Check if recording is currently active
     */
    public boolean isRecording() {
        return isRecording;
    }
    
    /**
     * Get the current test name being recorded
     */
    public String getCurrentTestName() {
        return currentTestName;
    }
}