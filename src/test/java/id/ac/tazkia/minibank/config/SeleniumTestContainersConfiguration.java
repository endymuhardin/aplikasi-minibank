package id.ac.tazkia.minibank.config;

import java.io.File;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat;

@TestConfiguration(proxyBeanMethods = false)
public class SeleniumTestContainersConfiguration {

    private static final File RECORDING_OUTPUT_FOLDER = new File("./target/selenium-recordings/");

    @SuppressWarnings("resource")
	@Bean
	BrowserWebDriverContainer<?> browserContainer(){
		RECORDING_OUTPUT_FOLDER.mkdirs();
		return new BrowserWebDriverContainer<>()
			.withAccessToHost(true)
    		.withCapabilities(new FirefoxOptions())
			.withRecordingMode(
				VncRecordingMode.RECORD_ALL, 
				RECORDING_OUTPUT_FOLDER,
				VncRecordingFormat.MP4);
	}

}