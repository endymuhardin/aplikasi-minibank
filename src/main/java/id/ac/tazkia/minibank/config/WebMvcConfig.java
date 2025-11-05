package id.ac.tazkia.minibank.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Configuration for web MVC including date formatting
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Register LocalDate formatter for HTML5 date input compatibility
        registry.addFormatter(new org.springframework.format.Formatter<LocalDate>() {

            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String print(LocalDate date, java.util.Locale locale) {
                if (date == null) {
                    return "";
                }
                return date.format(formatter);
            }

            @Override
            public LocalDate parse(String text, java.util.Locale locale) throws java.text.ParseException {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text, formatter);
                } catch (Exception e) {
                    throw new java.text.ParseException("Invalid date format. Please use yyyy-MM-dd format.", 0);
                }
            }
        });
    }
}