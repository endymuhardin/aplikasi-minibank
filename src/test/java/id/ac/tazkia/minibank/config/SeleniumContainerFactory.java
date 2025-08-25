package id.ac.tazkia.minibank.config;

import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.utility.DockerImageName;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class SeleniumContainerFactory {
    
    private static final String SELENIUM_CHROME_IMAGE = "selenium/standalone-chrome:4.15.0";
    private static final String SELENIARM_CHROME_IMAGE = "seleniarm/standalone-chromium:latest";
    
    /**
     * Creates appropriate Selenium container based on system architecture and profile
     */
    public static BrowserWebDriverContainer<?> createSeleniumContainer() {
        String testProfile = System.getProperty("test.profile", "local-m1");
        String architecture = System.getProperty("os.arch", "unknown").toLowerCase();
        boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
        boolean recording = Boolean.parseBoolean(System.getProperty("selenium.recording.enabled", "false"));
        
        BrowserWebDriverContainer<?> container;
        
        if ("remote".equals(testProfile)) {
            // Remote build server - use full resource configuration
            container = createRemoteContainer();
        } else if ("local-m1".equals(testProfile) || 
                   (architecture.contains("aarch64") || architecture.contains("arm64"))) {
            // M1/M2 Mac (ARM64) - use Seleniarm for better performance
            container = createArmContainer();
        } else {
            // Intel/AMD64 - use standard Selenium
            container = createStandardContainer();
        }
        
        return configureContainer(container, testProfile, headless, recording);
    }
    
    private static BrowserWebDriverContainer<?> createRemoteContainer() {
        log.info("Creating Selenium container for remote build server");
        return new BrowserWebDriverContainer<>(DockerImageName.parse(SELENIUM_CHROME_IMAGE))
                .withAccessToHost(true)
                .withSharedMemorySize(2147483648L) // 2GB shared memory
                .withEnv("JAVA_OPTS", "-Xmx1g")
                .withEnv("SE_NODE_MAX_INSTANCES", "4")
                .withEnv("SE_NODE_MAX_SESSIONS", "4");
    }
    
    private static BrowserWebDriverContainer<?> createArmContainer() {
        log.info("Creating Seleniarm container for ARM64 architecture");
        DockerImageName armImage = DockerImageName.parse(SELENIARM_CHROME_IMAGE)
                .asCompatibleSubstituteFor("selenium/standalone-chrome");
        return new BrowserWebDriverContainer<>(armImage)
                .withAccessToHost(true)
                .withSharedMemorySize(512000000L) // 512MB shared memory
                .withEnv("JAVA_OPTS", "-Xmx512m")
                .withEnv("SE_NODE_MAX_INSTANCES", "1")
                .withEnv("SE_NODE_MAX_SESSIONS", "1");
    }
    
    private static BrowserWebDriverContainer<?> createStandardContainer() {
        log.info("Creating standard Selenium container for x86_64 architecture");
        return new BrowserWebDriverContainer<>(DockerImageName.parse(SELENIUM_CHROME_IMAGE))
                .withAccessToHost(true)
                .withSharedMemorySize(1073741824L) // 1GB shared memory
                .withEnv("JAVA_OPTS", "-Xmx768m")
                .withEnv("SE_NODE_MAX_INSTANCES", "2")
                .withEnv("SE_NODE_MAX_SESSIONS", "2");
    }
    
    private static BrowserWebDriverContainer<?> configureContainer(BrowserWebDriverContainer<?> container, 
                                                                   String testProfile, boolean headless, boolean recording) {
        // Configure recording if enabled
        if (recording) {
            java.io.File recordingDir = new java.io.File("target/selenium-recordings/");
            // Ensure the directory exists
            if (!recordingDir.exists()) {
                boolean created = recordingDir.mkdirs();
                log.info("Created recording directory: {} (success: {})", recordingDir.getAbsolutePath(), created);
            }
            
            // Use simple recording mode without custom file factory to avoid timing issues
            container.withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, recordingDir);
            
            log.info("Recording enabled - mode: RECORD_ALL - directory: {}", recordingDir.getAbsolutePath());
            log.info("Note: Recordings may take time to finalize after test completion");
        }
        
        log.info("Selenium container configured for profile: {} | headless: {} | recording: {}", 
                testProfile, headless, recording);
        
        return container;
    }
    
    /**
     * Logs VNC connection information for viewing the browser during test execution
     */
    public static void logVncInformation(BrowserWebDriverContainer<?> container) {
        try {
            if (container.isRunning()) {
                String vncAddress = container.getVncAddress();
                
                log.info("═══════════════════════════════════════════════════════════");
                log.info("🖥️  VNC Viewer Information:");
                log.info("   Address: {}", vncAddress);
                log.info("   Password: secret (default VNC password)");
                log.info("   You can connect using any VNC client to view the browser");
                log.info("   Example: open {} (macOS) or use TigerVNC/RealVNC", vncAddress);
                log.info("   Note: If 'secret' doesn't work, try no password or 'noVNC'");
                log.info("═══════════════════════════════════════════════════════════");
            }
        } catch (Exception e) {
            log.debug("Could not retrieve VNC information: {}", e.getMessage());
        }
    }
}