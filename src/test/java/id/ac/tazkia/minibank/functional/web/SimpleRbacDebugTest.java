package id.ac.tazkia.minibank.functional.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openqa.selenium.By;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleRbacDebugTest extends BaseSeleniumTest {

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    public void debugUserPage() {
        String url = baseUrl + "/rbac/users/list";
        System.out.println("Navigating to: " + url);
        
        driver.get(url);
        
        // Wait a bit for page to load
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Print current URL
        System.out.println("Current URL: " + driver.getCurrentUrl());
        
        // Print page title
        System.out.println("Page title: " + driver.getTitle());
        
        // Print page source (first 1000 chars)
        String pageSource = driver.getPageSource();
        System.out.println("Page source preview: " + pageSource.substring(0, Math.min(1000, pageSource.length())));
        
        // Check if table exists
        try {
            driver.findElement(By.id("users-table"));
            System.out.println("SUCCESS: users-table found!");
        } catch (Exception e) {
            System.out.println("ERROR: users-table not found: " + e.getMessage());
        }
        
        // Just assert that we got some response
        assertFalse(pageSource.isEmpty());
    }
}