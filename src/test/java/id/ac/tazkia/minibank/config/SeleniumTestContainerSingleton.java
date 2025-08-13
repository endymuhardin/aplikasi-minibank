package id.ac.tazkia.minibank.config;

import java.io.File;
import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("resource")
public class SeleniumTestContainerSingleton {
    public static final String TESTCONTAINER_HOST_URL = "http://host.testcontainers.internal";
    private static final File RECORDING_OUTPUT_FOLDER = new File("./target/selenium-recordings/");
    
    private static BrowserWebDriverContainer<?> container;
    public static WebDriver driver;
    private static volatile boolean initialized = false;
    private static volatile int initializationAttempts = 0;
    private static volatile String firstInitializedBy = null;
    
    public static synchronized void initialize() {
        initializationAttempts++;
        String callerInfo = getCallerInfo();
        
        if (initialized) {
            log.info("🔄 SELENIUM CONTAINER REUSE: Container already initialized (attempt #{}) - called by {}. First initialized by: {}. ✅ EXPECTED BEHAVIOR", 
                    initializationAttempts, callerInfo, firstInitializedBy);
            return;
        }
        
        firstInitializedBy = callerInfo;
        log.info("🚀 SELENIUM CONTAINER INITIALIZATION: Starting FIRST initialization (attempt #{}) - called by: {}. ✅ EXPECTED BEHAVIOR", 
                initializationAttempts, callerInfo);
        
        try {
            String browser = System.getProperty("selenium.browser", "chrome").toLowerCase(); // Default to chrome for faster startup
            boolean recordingEnabled = Boolean.parseBoolean(System.getProperty("selenium.recording.enabled", "false"));
            boolean headlessEnabled = Boolean.parseBoolean(System.getProperty("selenium.headless", "true")); // Default to headless mode
            
            log.info("Initializing Selenium TestContainer with browser: {}, recording: {}, headless: {}", browser, recordingEnabled, headlessEnabled);
            
            container = new BrowserWebDriverContainer<>()
                .withAccessToHost(true)
                .waitingFor(Wait.forHttp("/wd/hub/status").forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(5)) // Increase timeout
                .withSharedMemorySize(2147483648L); // 2GB shared memory for better performance
                
            // Set browser capabilities
            switch (browser) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments(
                        "--no-sandbox",
                        "--disable-dev-shm-usage", 
                        "--disable-gpu",
                        "--disable-web-security",
                        "--disable-features=VizDisplayCompositor"
                    );
                    if (headlessEnabled) {
                        chromeOptions.addArguments("--headless");
                        log.info("Chrome headless mode enabled");
                    } else {
                        log.info("Chrome headless mode disabled - browser window will be visible");
                    }
                    container.withCapabilities(chromeOptions);
                    break;
                case "firefox":
                default:
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (headlessEnabled) {
                        firefoxOptions.addArguments("--headless");
                        log.info("Firefox headless mode enabled");
                    } else {
                        log.info("Firefox headless mode disabled - browser window will be visible");
                    }
                    container.withCapabilities(firefoxOptions);
                    break;
            }
            
            // Configure recording if enabled
            if (recordingEnabled) {
                RECORDING_OUTPUT_FOLDER.mkdirs();
                container.withRecordingMode(
                    VncRecordingMode.RECORD_ALL, 
                    RECORDING_OUTPUT_FOLDER,
                    VncRecordingFormat.MP4);
            }
            
            log.info("Starting Selenium container...");
            long startTime = System.currentTimeMillis();
            container.start();
            long endTime = System.currentTimeMillis();
            log.info("Selenium container started successfully in {}ms", endTime - startTime);
            log.info("Selenium URL: {}", container.getSeleniumAddress());
                    
            // Create WebDriver with the same options as the container
            switch (browser) {
                case "chrome":
                    ChromeOptions chromeDriverOptions = new ChromeOptions();
                    chromeDriverOptions.addArguments(
                        "--no-sandbox",
                        "--disable-dev-shm-usage", 
                        "--disable-gpu",
                        "--disable-web-security",
                        "--disable-features=VizDisplayCompositor"
                    );
                    if (headlessEnabled) {
                        chromeDriverOptions.addArguments("--headless");
                    }
                    driver = new RemoteWebDriver(container.getSeleniumAddress(), chromeDriverOptions);
                    break;
                case "firefox":
                default:
                    FirefoxOptions firefoxDriverOptions = new FirefoxOptions();
                    if (headlessEnabled) {
                        firefoxDriverOptions.addArguments("--headless");
                    }
                    driver = new RemoteWebDriver(container.getSeleniumAddress(), firefoxDriverOptions);
                    break;
            }
            
            // Configure WebDriver timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            
            // Verify WebDriver is responsive
            verifyWebDriverReadiness();
            
            log.info("✅ SELENIUM CONTAINER READY: Container initialized successfully by {} (attempt #{}/{} total attempts)", 
                    firstInitializedBy, 1, initializationAttempts);
            log.info("VNC URL: {}", container.getVncAddress());
            
            initialized = true;
            Runtime.getRuntime().addShutdownHook(new Thread(SeleniumTestContainerSingleton::cleanup));
                   
        } catch (Exception e) {
            log.error("❌ SELENIUM CONTAINER FAILED: Initialization failed on attempt #{} called by {}", 
                    initializationAttempts, callerInfo, e);
            throw new RuntimeException("Failed to initialize Selenium TestContainer", e);
        }
    }
    
    private static String getCallerInfo() {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        // Skip getStackTrace(), getCallerInfo(), and initialize() methods
        for (int i = 3; i < stack.length; i++) {
            StackTraceElement element = stack[i];
            String className = element.getClassName();
            // Return the first non-SeleniumTestContainerSingleton class
            if (!className.contains("SeleniumTestContainerSingleton")) {
                return String.format("%s.%s():%d", 
                    className.substring(className.lastIndexOf('.') + 1), 
                    element.getMethodName(), 
                    element.getLineNumber());
            }
        }
        return "Unknown";
    }
    
    public static BrowserWebDriverContainer<?> getContainer() {
        return container;
    }
    
    /**
     * Returns statistics about container initialization for debugging
     */
    public static String getInitializationStats() {
        return String.format("Initialized: %s, Attempts: %d, First by: %s", 
                initialized, initializationAttempts, firstInitializedBy);
    }
    
    /**
     * Logs current container status - useful for troubleshooting
     */
    public static void logCurrentStatus() {
        log.info("📊 CONTAINER STATUS: {} | Container: {} | Driver: {}", 
                getInitializationStats(),
                container != null ? "ACTIVE" : "NULL",
                driver != null ? "AVAILABLE" : "NULL");
    }
    
    private static void verifyWebDriverReadiness() {
        try {
            log.info("Verifying WebDriver readiness...");
            // Simple check to ensure WebDriver is responsive
            String title = driver.getTitle();
            log.info("WebDriver is ready, current page title: '{}'", title);
        } catch (Exception e) {
            log.warn("WebDriver readiness check failed, but continuing: {}", e.getMessage());
        }
    }
    
    private static void cleanup() {
        log.info("🧹 SELENIUM CONTAINER CLEANUP: Starting cleanup process (was initialized by {}, {} total attempts)", 
                firstInitializedBy != null ? firstInitializedBy : "Unknown", initializationAttempts);
        
        if (driver != null) {
            try {
                driver.quit();
                log.info("✅ WebDriver closed successfully");
            } catch (Exception e) {
                log.warn("⚠️ Error closing WebDriver: {}", e.getMessage());
            }
        }
        
        if (container != null) {
            try {
                container.stop();
                log.info("✅ SELENIUM CONTAINER CLEANUP COMPLETE: Container stopped successfully after {} initialization attempts", 
                        initializationAttempts);
            } catch (Exception e) {
                log.warn("⚠️ Error stopping container: {}", e.getMessage());
            }
        }
    }
}
