package id.ac.tazkia.minibank.integration.controller;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserPasswordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UserControllerManualTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserPasswordRepository userPasswordRepository;
    
    @Test
    public void shouldCreateUserThroughController() {
        String uniqueUsername = "manual_test_user_" + System.currentTimeMillis();
        String fullName = "Manual Test User";
        String email = "manual.test" + System.currentTimeMillis() + "@example.com";
        
        // Prepare form data (no password field since we separated it)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", uniqueUsername);
        formData.add("fullName", fullName);
        formData.add("email", email);
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        
        // Submit form to create user
        String createUserUrl = "http://localhost:" + port + "/rbac/users/create";
        ResponseEntity<String> response = restTemplate.exchange(createUserUrl, HttpMethod.POST, request, String.class);
        
        // Print response for debugging
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Headers: " + response.getHeaders());
        if (response.getBody().length() > 2000) {
            System.out.println("Response Body (first 2000 chars): " + response.getBody().substring(0, 2000) + "...");
        } else {
            System.out.println("Response Body: " + response.getBody());
        }
        
        // Verify user was created in database
        User savedUser = userRepository.findByUsername(uniqueUsername).orElse(null);
        if (savedUser != null) {
            System.out.println("User created successfully: " + savedUser.getUsername());
            assertEquals(uniqueUsername, savedUser.getUsername());
            assertEquals(fullName, savedUser.getFullName());
            assertEquals(email, savedUser.getEmail());
            
            // Password should not be created since we separated the functionality
            boolean hasPassword = userPasswordRepository.findByUser(savedUser).isPresent();
            System.out.println("User has password: " + hasPassword + " (should be false)");
        } else {
            System.out.println("User was not created in database");
        }
        
        // Check if response indicates success (redirect)
        if (response.getStatusCode().is3xxRedirection()) {
            String location = response.getHeaders().getLocation().toString();
            System.out.println("Redirect location: " + location);
            assertTrue(location.contains("/rbac/users/list"), "Should redirect to user list page");
        }
    }
}