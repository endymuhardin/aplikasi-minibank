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
    
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        
        try {
            String browser = System.getProperty("selenium.browser", "chrome").toLowerCase(); // Default to chrome for faster startup
            boolean recordingEnabled = Boolean.parseBoolean(System.getProperty("selenium.recording.enabled", "false"));
            
            log.info("Initializing Selenium TestContainer with browser: {} and recording: {}", browser, recordingEnabled);
            
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
                        "--disable-features=VizDisplayCompositor",
                        "--headless" // Run in headless mode for faster startup
                    );
                    container.withCapabilities(chromeOptions);
                    break;
                case "firefox":
                default:
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.addArguments("--headless"); // Run in headless mode
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
                        "--disable-features=VizDisplayCompositor",
                        "--headless"
                    );
                    driver = new RemoteWebDriver(container.getSeleniumAddress(), chromeDriverOptions);
                    break;
                case "firefox":
                default:
                    FirefoxOptions firefoxDriverOptions = new FirefoxOptions();
                    firefoxDriverOptions.addArguments("--headless");
                    driver = new RemoteWebDriver(container.getSeleniumAddress(), firefoxDriverOptions);
                    break;
            }
            
            // Configure WebDriver timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            
            // Verify WebDriver is responsive
            verifyWebDriverReadiness();
            
            log.info("Selenium TestContainer initialized successfully");
            log.info("VNC URL: {}", container.getVncAddress());
            
            initialized = true;
            Runtime.getRuntime().addShutdownHook(new Thread(SeleniumTestContainerSingleton::cleanup));
                   
        } catch (Exception e) {
            log.error("Failed to initialize Selenium TestContainer", e);
            throw new RuntimeException("Failed to initialize Selenium TestContainer", e);
        }
    }
    
    public static BrowserWebDriverContainer<?> getContainer() {
        return container;
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
        if (driver != null) {
            try {
                driver.quit();
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
