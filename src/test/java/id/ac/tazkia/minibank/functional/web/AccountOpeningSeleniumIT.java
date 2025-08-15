package id.ac.tazkia.minibank.functional.web;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AccountOpeningSeleniumIT extends BaseSeleniumTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testOpenPersonalAccount_HappyPath() {
        driver.get(baseUrl + "/account/open/personal");

        // Find an active product to use for the test
        Optional<Product> productOpt = productRepository.findByIsActiveTrue().stream().findFirst();
        Assertions.assertTrue(productOpt.isPresent(), "At least one active product should exist for testing");
        Product product = productOpt.get();

        // Fill out the form
        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("email")).sendKeys("john.doe@test.com");
        driver.findElement(By.id("phoneNumber")).sendKeys("081234567890");
        driver.findElement(By.id("dateOfBirth")).sendKeys(LocalDate.of(1990, 1, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        new Select(driver.findElement(By.id("identityType"))).selectByValue("KTP");
        driver.findElement(By.id("identityNumber")).sendKeys("3201010101010005");
        driver.findElement(By.id("address")).sendKeys("123 Test Street");
        driver.findElement(By.id("city")).sendKeys("Testville");
        driver.findElement(By.id("postalCode")).sendKeys("12345");
        new Select(driver.findElement(By.id("productId"))).selectByValue(product.getId().toString());
        driver.findElement(By.id("initialDeposit")).sendKeys("100000");

        // Submit the form
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        // Wait for and verify the success message
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(.,'Personal account opened successfully!')]")));
        Assertions.assertTrue(successMessage.isDisplayed());
        log.info("✅ TEST PASS: Happy path test for personal account opening completed successfully.");
    }

    @Test
    void testOpenPersonalAccount_ValidationErrors() {
        driver.get(baseUrl + "/account/open/personal");

        // Submit the form with empty fields
        driver.findElement(By.xpath("//button[@type='submit']")).click();

        // Wait for the page to reload and check for error messages
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//p[contains(text(), 'First name is required')]")));

        // Verify that all expected error messages are present
        List<WebElement> errorMessages = driver.findElements(By.xpath("//p[contains(@class, 'text-red-500')]"));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("First name is required")));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("Last name is required")));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("Email is required")));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("Phone number is required")));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("Date of birth is required")));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("Identity number is required")));
        Assertions.assertTrue(errorMessages.stream().anyMatch(el -> el.getText().contains("Product ID is required")));

        log.info("✅ TEST PASS: Validation error test for personal account opening completed successfully.");
    }
}
