package id.ac.tazkia.minibank.config;

import java.io.File;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat;
import org.testcontainers.containers.wait.strategy.Wait;

import lombok.extern.slf4j.Slf4j;

/**
 * Thread-safe Selenium WebDriver manager for parallel test execution.
 * Creates isolated WebDriver instances per thread to avoid conflicts.
 */
@Slf4j
@Component
public class ParallelSeleniumManager {
    
    // Static reference to configuration - set during initialization
    private static SeleniumTestProperties properties;
    
    public static final String TESTCONTAINER_HOST_URL = "http://host.testcontainers.internal";
    private static final File RECORDING_OUTPUT_FOLDER = new File("./target/selenium-recordings/");
    
    // Thread-local storage for WebDriver instances
    private static final ThreadLocal<WebDriverInstance> threadLocalDriver = new ThreadLocal<>();
    
    // Container pool management
    private static final Map<String, BrowserWebDriverContainer<?>> containerPool = new ConcurrentHashMap<>();
    private static final AtomicInteger containerCounter = new AtomicInteger(0);
    
    // Container startup throttling - initialized lazily
    private static volatile Semaphore containerStartupSemaphore;
    
    // Configuration
    private static final String BROWSER_TYPE = System.getProperty("selenium.browser", "chrome").toLowerCase();
    private static final boolean RECORDING_ENABLED = Boolean.parseBoolean(System.getProperty("selenium.recording.enabled", "false"));
    private static final boolean HEADLESS_ENABLED = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
    
    /**
     * Initialize the manager with configuration properties.
     * This must be called before using any static methods.
     */
    @Autowired
    public void setProperties(SeleniumTestProperties properties) {
        ParallelSeleniumManager.properties = properties;
        // Initialize semaphore with configured max containers
        if (containerStartupSemaphore == null) {
            synchronized (ParallelSeleniumManager.class) {
                if (containerStartupSemaphore == null) {
                    containerStartupSemaphore = new Semaphore(properties.getEffectiveMaxContainers(), true);
                }
            }
        }
        log.info("🔧 SELENIUM CONFIGURATION: Initialized with maxContainers={}, memoryMB={}, cpuQuota={}", 
                properties.getEffectiveMaxContainers(),
                properties.getContainer().getMemoryLimitMb(),
                properties.getContainer().getCpuQuota());
    }
    
    /**
     * Get current configuration, initialize with defaults if not set
     */
    private static SeleniumTestProperties getProperties() {
        if (properties == null) {
            // Fallback to default configuration
            properties = new SeleniumTestProperties();
            if (containerStartupSemaphore == null) {
                synchronized (ParallelSeleniumManager.class) {
                    if (containerStartupSemaphore == null) {
                        containerStartupSemaphore = new Semaphore(properties.getEffectiveMaxContainers(), true);
                    }
                }
            }
        }
        return properties;
    }
    
    /**
     * Get WebDriver instance for current thread
     */
    public static WebDriver getDriver() {
        WebDriverInstance instance = threadLocalDriver.get();
        if (instance == null) {
            instance = createWebDriverInstance();
            threadLocalDriver.set(instance);
        }
        return instance.getDriver();
    }
    
    /**
     * Get base URL for current thread
     */
    public static String getBaseUrl(int webappPort) {
        WebDriverInstance instance = threadLocalDriver.get();
        if (instance != null) {
            return TESTCONTAINER_HOST_URL + ":" + webappPort;
        }
        throw new IllegalStateException("No WebDriver instance for current thread");
    }
    
    /**
     * Cleanup WebDriver for current thread
     */
    public static void cleanupCurrentThread() {
        WebDriverInstance instance = threadLocalDriver.get();
        if (instance != null) {
            try {
                instance.cleanup();
                threadLocalDriver.remove();
                log.debug("Cleaned up WebDriver for thread: {}", Thread.currentThread().getName());
            } catch (Exception e) {
                log.warn("Error cleaning up WebDriver for thread {}: {}", 
                        Thread.currentThread().getName(), e.getMessage());
            }
        }
    }
    
    /**
     * Reset browser state for current thread
     */
    public static void resetBrowserState() {
        WebDriverInstance instance = threadLocalDriver.get();
        if (instance != null) {
            instance.resetBrowserState();
        }
    }
    
    /**
     * Create new WebDriver instance with dedicated container
     */
    private static WebDriverInstance createWebDriverInstance() {
        String threadName = Thread.currentThread().getName();
        String containerId = "selenium-" + containerCounter.getAndIncrement() + "-" + threadName.replaceAll("[^a-zA-Z0-9]", "");
        
        log.info("Creating WebDriver instance for thread: {} [Container: {}]", threadName, containerId);
        
        try {
            // Acquire semaphore to throttle container startup
            log.debug("Waiting for container startup permit... [Thread: {}, Available permits: {}]", 
                     threadName, containerStartupSemaphore.availablePermits());
            containerStartupSemaphore.acquire();
            
            try {
                BrowserWebDriverContainer<?> container = createContainer(containerId);
                
                // Start container - fail fast if it doesn't work
                container.start();
                log.info("Container started successfully for thread: {} [Container: {}]", threadName, containerId);
                
                WebDriver driver = createDriver(container);
                WebDriverInstance instance = new WebDriverInstance(containerId, container, driver);
                
                // Store container in pool for monitoring
                containerPool.put(containerId, container);
                
                log.info("WebDriver instance created successfully for thread: {} [Container: {}, VNC: {}]", 
                        threadName, containerId, container.getVncAddress());
                
                return instance;
                
            } finally {
                // Release semaphore after container is started
                containerStartupSemaphore.release();
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while waiting for container startup permit for thread: {}", threadName, e);
            throw new RuntimeException("Container startup interrupted", e);
        } catch (Exception e) {
            log.error("Failed to create WebDriver instance for thread: {}", threadName, e);
            throw new RuntimeException("Failed to create WebDriver instance", e);
        }
    }
    
    /**
     * Create optimized browser container
     */
    private static BrowserWebDriverContainer<?> createContainer(String containerId) {
        SeleniumTestProperties props = getProperties();
        log.debug("Creating container: {} with browser: {}, headless: {}", containerId, BROWSER_TYPE, HEADLESS_ENABLED);
        
        Duration startupTimeout = Duration.ofSeconds(props.getContainer().getStartupTimeoutSeconds());
        long memoryLimitBytes = props.getContainer().getMemoryLimitMb() * 1024 * 1024;
        long sharedMemoryBytes = props.getContainer().getSharedMemoryBytes();
        
        BrowserWebDriverContainer<?> container = new BrowserWebDriverContainer<>()
                .withAccessToHost(true)
                .waitingFor(Wait.forHttp("/wd/hub/status").forStatusCode(200).withStartupTimeout(startupTimeout))
                .withStartupTimeout(startupTimeout)
                .withSharedMemorySize(sharedMemoryBytes)
                .withCreateContainerCmdModifier(cmd -> {
                    // Configure resource limits from properties
                    cmd.getHostConfig().withCpuQuota(props.getContainer().getCpuQuota());
                    cmd.getHostConfig().withMemory(memoryLimitBytes);
                    cmd.getHostConfig().withShmSize(sharedMemoryBytes);
                });
        
        // Set browser capabilities
        switch (BROWSER_TYPE) {
            case "chrome":
                container.withCapabilities(createChromeOptions());
                break;
            case "firefox":
            default:
                container.withCapabilities(createFirefoxOptions());
                break;
        }
        
        // Configure recording if enabled
        if (RECORDING_ENABLED) {
            RECORDING_OUTPUT_FOLDER.mkdirs();
            File containerRecordingDir = new File(RECORDING_OUTPUT_FOLDER, containerId);
            containerRecordingDir.mkdirs();
            
            container.withRecordingMode(
                VncRecordingMode.RECORD_ALL,
                containerRecordingDir,
                VncRecordingFormat.MP4);
        }
        
        return container;
    }
    
    /**
     * Create optimized Chrome options
     */
    private static ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        
        options.addArguments(
            // Security and stability
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-web-security",
            "--disable-features=VizDisplayCompositor",
            // Performance optimizations for parallel execution
            "--disable-extensions",
            "--disable-plugins",
            "--disable-images", // Faster page loads
            "--disable-default-apps",
            "--disable-background-timer-throttling",
            "--disable-renderer-backgrounding",
            "--disable-backgrounding-occluded-windows",
            "--disable-client-side-phishing-detection",
            "--disable-sync",
            "--disable-translate",
            "--hide-scrollbars",
            "--metrics-recording-only",
            "--mute-audio",
            "--no-default-browser-check",
            "--no-first-run",
            "--safebrowsing-disable-auto-update",
            "--ignore-ssl-errors",
            "--ignore-certificate-errors",
            "--allow-running-insecure-content",
            "--disable-blink-features=AutomationControlled",
            // Memory optimization for parallel execution
            "--aggressive-cache-discard",
            "--memory-pressure-off",
            "--max-old-space-size=256" // Limit memory usage
        );
        
        if (HEADLESS_ENABLED) {
            options.addArguments("--headless");
        }
        
        return options;
    }
    
    /**
     * Create optimized Firefox options
     */
    private static FirefoxOptions createFirefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        
        // Firefox performance optimizations
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("media.volume_scale", "0.0");
        options.addPreference("dom.push.enabled", false);
        options.addPreference("dom.webdriver.enabled", false);
        options.addPreference("useAutomationExtension", false);
        options.addPreference("general.useragent.override", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        // Memory optimization
        options.addPreference("browser.cache.memory.capacity", 32768); // 32MB cache
        options.addPreference("browser.sessionhistory.max_total_viewers", 2);
        
        if (HEADLESS_ENABLED) {
            options.addArguments("--headless");
        }
        
        return options;
    }
    
    /**
     * Create WebDriver with configurable settings
     */
    private static WebDriver createDriver(BrowserWebDriverContainer<?> container) {
        SeleniumTestProperties props = getProperties();
        WebDriver driver;
        
        switch (BROWSER_TYPE) {
            case "chrome":
                driver = new RemoteWebDriver(container.getSeleniumAddress(), createChromeOptions());
                break;
            case "firefox":
            default:
                driver = new RemoteWebDriver(container.getSeleniumAddress(), createFirefoxOptions());
                break;
        }
        
        // Configure timeouts from properties
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(props.getTimeouts().getImplicitWaitSeconds()));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(props.getTimeouts().getPageLoadTimeoutSeconds()));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(props.getTimeouts().getScriptTimeoutSeconds()));
        
        // Maximize window for consistent behavior
        driver.manage().window().maximize();
        
        return driver;
    }
    
    /**
     * Calculate optimal number of containers based on system resources
     */
    private static int getMaxContainers() {
        int processors = Runtime.getRuntime().availableProcessors();
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        
        // Much more conservative approach to prevent resource exhaustion
        int maxByProcessors = Math.max(1, processors / 4); // Use 1/4 of processors instead of 1/2
        int maxByMemory = Math.max(1, (int) (maxMemoryMB / 2048)); // Require 2GB per container instead of 1GB
        
        int maxContainers = Math.min(maxByProcessors, maxByMemory);
        maxContainers = Math.min(maxContainers, 3); // Cap at 3 containers max instead of 8
        maxContainers = Math.max(maxContainers, 1); // Ensure at least 1 container
        
        log.info("Parallel Selenium configuration: {} processors, {}MB memory, {} max containers", 
                processors, maxMemoryMB, maxContainers);
        
        return maxContainers;
    }
    
    /**
     * Global cleanup - called during shutdown
     */
    public static void globalCleanup() {
        log.info("Starting global Selenium cleanup - {} active containers", containerPool.size());
        
        containerPool.values().parallelStream().forEach(container -> {
            try {
                container.stop();
            } catch (Exception e) {
                log.warn("Error stopping container: {}", e.getMessage());
            }
        });
        
        containerPool.clear();
        log.info("Global Selenium cleanup completed");
    }
    
    /**
     * Get container statistics
     */
    public static String getContainerStatistics() {
        return String.format("Active containers: %d/%d, Browser: %s, Headless: %s", 
                containerPool.size(), getProperties().getEffectiveMaxContainers(), BROWSER_TYPE, HEADLESS_ENABLED);
    }
    
    /**
     * WebDriver instance wrapper
     */
    private static class WebDriverInstance {
        private final String containerId;
        private final BrowserWebDriverContainer<?> container;
        private final WebDriver driver;
        private final long createdTime;
        
        public WebDriverInstance(String containerId, BrowserWebDriverContainer<?> container, WebDriver driver) {
            this.containerId = containerId;
            this.container = container;
            this.driver = driver;
            this.createdTime = System.currentTimeMillis();
        }
        
        public WebDriver getDriver() {
            return driver;
        }
        
        public void resetBrowserState() {
            try {
                // Clear browser state for test isolation
                driver.manage().deleteAllCookies();
                
                if (driver instanceof JavascriptExecutor) {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    js.executeScript("localStorage.clear();");
                    js.executeScript("sessionStorage.clear();");
                }
                
                // Navigate to blank page
                driver.get("data:text/html,<html><body><h1>Test Reset</h1></body></html>");
                
                log.debug("Browser state reset for container: {}", containerId);
            } catch (Exception e) {
                log.warn("Failed to reset browser state for container {}: {}", containerId, e.getMessage());
            }
        }
        
        public void cleanup() {
            try {
                if (driver != null) {
                    driver.quit();
                }
                
                if (container != null) {
                    container.stop();
                }
                
                // Remove from pool
                containerPool.remove(containerId);
                
                long lifeTime = System.currentTimeMillis() - createdTime;
                log.debug("WebDriver instance cleaned up: {} (lifetime: {}ms)", containerId, lifeTime);
                
            } catch (Exception e) {
                log.warn("Error during cleanup of WebDriver instance {}: {}", containerId, e.getMessage());
            }
        }
    }
    
    // Static block to register shutdown hook
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(ParallelSeleniumManager::globalCleanup));
    }
}