package id.ac.tazkia.minibank.config;

import java.io.File;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.BrowserWebDriverContainer.VncRecordingMode;
import org.testcontainers.containers.VncRecordingContainer.VncRecordingFormat;

@TestConfiguration(proxyBeanMethods = false)
public class SeleniumTestContainersConfiguration {

    private static final File RECORDING_OUTPUT_FOLDER = new File("./target/selenium-recordings/");

    @Value("${selenium.recording.enabled:false}")
    private boolean recordingEnabled;

    @SuppressWarnings("resource")
	@Bean
	BrowserWebDriverContainer<?> browserContainer(){
		System.out.println("Selenium recording enabled: " + recordingEnabled);
		
		BrowserWebDriverContainer<?> container = new BrowserWebDriverContainer<>()
			.withAccessToHost(true)
    		.withCapabilities(new FirefoxOptions());
		
		if (recordingEnabled) {
			System.out.println("Initializing Selenium recording to: " + RECORDING_OUTPUT_FOLDER.getAbsolutePath());
			RECORDING_OUTPUT_FOLDER.mkdirs();
			container.withRecordingMode(
				VncRecordingMode.RECORD_ALL, 
				RECORDING_OUTPUT_FOLDER,
				VncRecordingFormat.MP4);
		} else {
			System.out.println("Selenium recording is disabled");
		}
		
		return container;
	}

}