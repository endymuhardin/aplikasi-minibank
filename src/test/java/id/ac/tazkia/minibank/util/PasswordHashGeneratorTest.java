package id.ac.tazkia.minibank.util;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

@SpringBootTest
@Import({PostgresTestContainersConfiguration.class})
@ActiveProfiles("test")
public class PasswordHashGeneratorTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("classpath:/fixtures/selenium/login_test_data.csv")
    private Resource loginTestData;

    @Test
    public void generatePasswordHash() {
        String password = "minibank123";
        String hash = passwordEncoder.encode(password);
        
        System.out.println("Password: " + password);
        System.out.println("BCrypt hash: " + hash);
        
        // Test if the hash matches
        boolean matches = passwordEncoder.matches(password, hash);
        System.out.println("Hash matches password: " + matches);
        
        // Test the existing hash from database
        String existingHash = "$2a$10$6tjICoD1DhK3r82bD4NiSuJ8A4xvf5osh96V7Q4BXFvIXZB3/s7da";
        boolean existingMatches = passwordEncoder.matches(password, existingHash);
        System.out.println("Existing hash matches password: " + existingMatches);
    }

    @Test
    public void generateBcryptHashFromCsv() throws IOException, CsvException {
        try (CSVReader reader = new CSVReader(new InputStreamReader(loginTestData.getInputStream()))) {
            reader.readNext(); // Skip header
            List<String[]> records = reader.readAll();
            int hashGeneratedCount = 0;
            for (String[] record : records) {
                if (Boolean.parseBoolean(record[4])) {
                    String username = record[0];
                    String plainPassword = record[3];
                    String hashedPassword = passwordEncoder.encode(plainPassword);
                    System.out.println("Username: " + username + ", Plain Password: " + plainPassword + ", Hashed Password: " + hashedPassword);
                    hashGeneratedCount++;
                }
            }
            assert hashGeneratedCount > 0 : "Should generate at least one password hash";
        }
    }
}
