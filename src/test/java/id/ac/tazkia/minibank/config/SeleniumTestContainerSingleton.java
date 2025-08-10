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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("resource")
public class SeleniumTestContainerSingleton {
    public static final String TESTCONTAINER_HOST_URL = "http://host.testcontainers.internal";
    private static final File RECORDING_OUTPUT_FOLDER = new File("./target/selenium-recordings/");
    
    private static BrowserWebDriverContainer<?> container;
    public static WebDriver DRIVER;
    
    static {
        initialize();
        Runtime.getRuntime().addShutdownHook(new Thread(SeleniumTestContainerSingleton::cleanup));
    }
    
    private static void initialize() {
        try {
            String browser = System.getProperty("selenium.browser", "firefox").toLowerCase();
            boolean recordingEnabled = Boolean.parseBoolean(System.getProperty("selenium.recording.enabled", "false"));
            
            log.info("Initializing Selenium TestContainer with browser: {} and recording: {}", browser, recordingEnabled);
            
            container = new BrowserWebDriverContainer<>()
                .withAccessToHost(true);
                
            // Set browser capabilities
            switch (browser) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
                    container.withCapabilities(chromeOptions);
                    break;
                case "firefox":
                default:
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
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
            container.start();
            log.info("Selenium container started, Selenium URL: {}", container.getSeleniumAddress());
            
            // Wait a moment for container to be fully ready
            Thread.sleep(2000);
            
            // Initialize WebDriver with retry logic
            int maxRetries = 3;
            Exception lastException = null;
            
            for (int retry = 0; retry < maxRetries; retry++) {
                try {
                    log.info("Attempting to create WebDriver (attempt {} of {})", retry + 1, maxRetries);
                    
                    switch (browser) {
                        case "chrome":
                            ChromeOptions chromeOptions = new ChromeOptions();
                            chromeOptions.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu");
                            DRIVER = new RemoteWebDriver(container.getSeleniumAddress(), chromeOptions);
                            break;
                        case "firefox":
                        default:
                            FirefoxOptions firefoxOptions = new FirefoxOptions();
                            DRIVER = new RemoteWebDriver(container.getSeleniumAddress(), firefoxOptions);
                            break;
                    }
                    
                    DRIVER.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
                    
                    log.info("Selenium TestContainer initialized successfully");
                    log.info("VNC URL: {}", container.getVncAddress());
                    return;
                    
                } catch (Exception e) {
                    lastException = e;
                    log.warn("Failed to create WebDriver on attempt {}: {}", retry + 1, e.getMessage());
                    if (retry < maxRetries - 1) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted while waiting to retry WebDriver creation", ie);
                        }
                    }
                }
            }
            
            throw new RuntimeException("Failed to initialize WebDriver after " + maxRetries + " attempts", lastException);
            
        } catch (Exception e) {
            log.error("Failed to initialize Selenium TestContainer", e);
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static BrowserWebDriverContainer<?> getContainer() {
        return container;
    }
    
    private static void cleanup() {
        if (DRIVER != null) {
            try {
                DRIVER.quit();
                log.info("WebDriver closed successfully");
            } catch (Exception e) {
                log.warn("Error closing WebDriver: {}", e.getMessage());
            }
        }
        
        if (container != null) {
            try {
                container.stop();
                log.info("Selenium container stopped successfully");
            } catch (Exception e) {
                log.warn("Error stopping container: {}", e.getMessage());
            }
        }
    }
}
