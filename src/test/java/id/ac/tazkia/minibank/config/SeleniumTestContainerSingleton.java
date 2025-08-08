package id.ac.tazkia.minibank.config;

import java.io.File;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;

@SuppressWarnings("resource")
public class SeleniumTestContainerSingleton {
    public static final BrowserWebDriverContainer<?> BROWSER_CONTAINER;
    public static final RemoteWebDriver DRIVER;

    static {

        boolean debugMode = Boolean.parseBoolean(
                System.getenv().getOrDefault("SELENIUM_DEBUG", "false"));
        String browserName = System.getenv().getOrDefault("BROWSER", "chrome").toLowerCase();

        BROWSER_CONTAINER = new BrowserWebDriverContainer<>()
                .withCapabilities(createOptionsFor(browserName))
                .withAccessToHost(true)
                .withExtraHost("host.testcontainers.internal", "host-gateway");

        if (debugMode) {
            BROWSER_CONTAINER
                    .withRecordingMode(
                            BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL,
                            new File("./target/selenium-recordings"))
                    .withExposedPorts(5900); // VNC live view
        } else {
            BROWSER_CONTAINER
                    .withRecordingMode(
                            BrowserWebDriverContainer.VncRecordingMode.SKIP,
                            new File("./target"));
        }

        BROWSER_CONTAINER.start();

        System.out.println(">>> Selenium container started.");
        System.out.println(">>> WebDriver URI: " + BROWSER_CONTAINER.getSeleniumAddress());
        if (debugMode) {
            System.out.println(">>> VNC debug mode ON. Connect: " +
                    BROWSER_CONTAINER.getHost() + ":" + BROWSER_CONTAINER.getMappedPort(5900) +
                    " (password: secret)");
        } else {
            System.out.println(">>> VNC debug mode OFF.");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(BROWSER_CONTAINER::stop));

        URL seleniumGridUrl = BROWSER_CONTAINER.getSeleniumAddress();
        DRIVER = new RemoteWebDriver(seleniumGridUrl, new ChromeOptions());

        Runtime.getRuntime().addShutdownHook(new Thread(DRIVER::quit));
    }

    private static Capabilities createOptionsFor(String browser) {
        switch (browser) {
            case "firefox":
                return new FirefoxOptions();
            case "chrome":
            default:
                return new ChromeOptions();
        }
    }
}
