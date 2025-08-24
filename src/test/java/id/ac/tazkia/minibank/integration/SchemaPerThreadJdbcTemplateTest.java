package id.ac.tazkia.minibank.integration;

import id.ac.tazkia.minibank.config.BaseIntegrationTest;
import id.ac.tazkia.minibank.config.TestDataFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Execution(ExecutionMode.CONCURRENT)
class SchemaPerThreadJdbcTemplateTest extends BaseIntegrationTest {
    
    // Test data for lifecycle methods
    private String lifecycleBranchCode;
    private String lifecycleBranchName;
    private String threadMarker;
    
    @BeforeEach
    void setUpTestData() {
        // Create unique thread marker for this test execution
        threadMarker = TestDataFactory.generateThreadMarker();
        
        // Set up test data for lifecycle verification
        lifecycleBranchCode = TestDataFactory.generateLifecycleCode();
        lifecycleBranchName = TestDataFactory.generateBranchName();
        
        // Insert the lifecycle test data that will be used by update/delete tests
        jdbcTemplate.update(
            "INSERT INTO branches (branch_code, branch_name, address, city, created_by) VALUES (?, ?, ?, ?, ?)",
            lifecycleBranchCode, lifecycleBranchName, 
            TestDataFactory.generateIndonesianAddress(), 
            TestDataFactory.generateIndonesianCity(), 
            threadMarker
        );
        
        log.info("@BeforeEach: Set up lifecycle test data {} for schema: {} on thread: {} with marker: {}", 
                lifecycleBranchCode, schemaName, Thread.currentThread().getName(), threadMarker);
    }
    
    @AfterEach
    void cleanUpTestData() {
        // Clean up test data for this specific thread (preserve migration data and other threads' data)
        try {
            int deletedRows = jdbcTemplate.update("DELETE FROM branches WHERE created_by = ?", threadMarker);
            if (deletedRows > 0) {
                log.info("@AfterEach: Cleaned up {} test data rows for thread marker {} from schema: {} (preserved migration data)", 
                        deletedRows, threadMarker, schemaName);
            } else {
                log.info("@AfterEach: No test data cleanup needed for thread marker {} in schema: {}", 
                        threadMarker, schemaName);
            }
        } catch (Exception e) {
            log.warn("Failed to clean up test data for thread marker {} in schema {}: {}", threadMarker, schemaName, e.getMessage());
        }
    }
    
    @Test
    void shouldCreateBranchSuccessfully() {
        // Given
        String branchCode = TestDataFactory.generateBranchCode();
        String branchName = TestDataFactory.generateBranchName();
        String address = TestDataFactory.generateIndonesianAddress();
        String city = TestDataFactory.generateIndonesianCity();
        String postalCode = TestDataFactory.generateIndonesianPostalCode();
        String phoneNumber = TestDataFactory.generateIndonesianPhoneNumber();
        String email = TestDataFactory.generateProfessionalEmail();
        String managerName = TestDataFactory.generateManagerName();
        
        // When
        String sql = """
            INSERT INTO branches (branch_code, branch_name, address, city, postal_code, 
                                phone_number, email, manager_name, status, is_main_branch, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE', false, ?)
            """;
        
        int rowsAffected = jdbcTemplate.update(sql, branchCode, branchName, address, city, 
                postalCode, phoneNumber, email, managerName, threadMarker);
        
        // Then
        assertEquals(1, rowsAffected);
        
        // Verify the inserted data
        String selectSql = "SELECT * FROM branches WHERE branch_code = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(selectSql, branchCode);
        
        assertNotNull(result);
        assertEquals(branchCode, result.get("branch_code"));
        assertEquals(branchName, result.get("branch_name"));
        assertEquals(address, result.get("address"));
        assertEquals(city, result.get("city"));
        assertEquals(postalCode, result.get("postal_code"));
        assertEquals(phoneNumber, result.get("phone_number"));
        assertEquals(email, result.get("email"));
        assertEquals(managerName, result.get("manager_name"));
        assertEquals("ACTIVE", result.get("status"));
        assertFalse((Boolean) result.get("is_main_branch"));
        assertEquals(threadMarker, result.get("created_by"));
        assertNotNull(result.get("id"));
        assertNotNull(result.get("created_date"));
    }
    
    @Test
    void shouldReadBranchByCode() {
        // Given - Use migration data (HO001 - Kantor Pusat Jakarta)
        String branchCode = "HO001";
        String expectedBranchName = "Kantor Pusat Jakarta";
        
        // When
        String sql = "SELECT * FROM branches WHERE branch_code = ?";
        Map<String, Object> result = jdbcTemplate.queryForMap(sql, branchCode);
        
        // Then
        assertNotNull(result);
        assertEquals(branchCode, result.get("branch_code"));
        assertEquals(expectedBranchName, result.get("branch_name"));
        assertEquals("SYSTEM", result.get("created_by"));
        assertTrue((Boolean) result.get("is_main_branch"));
        
        log.info("shouldReadBranchByCode: Successfully read migration data for {} from schema {}", 
                branchCode, schemaName);
    }
    
    @Test
    void shouldUpdateBranchSuccessfully() {
        // Given - Lifecycle data already prepared in @BeforeEach
        // Verify the initial data exists
        String selectSql = "SELECT * FROM branches WHERE branch_code = ?";
        Map<String, Object> initialData = jdbcTemplate.queryForMap(selectSql, lifecycleBranchCode);
        assertEquals(lifecycleBranchCode, initialData.get("branch_code"));
        assertEquals(lifecycleBranchName, initialData.get("branch_name"));
        assertEquals(threadMarker, initialData.get("created_by"));
        
        // When - Update the branch
        String newName = TestDataFactory.generateBranchName() + " Updated";
        String newManager = TestDataFactory.generateManagerName();
        
        String updateSql = """
            UPDATE branches 
            SET branch_name = ?, manager_name = ?, updated_by = ?, 
                updated_date = CURRENT_TIMESTAMP
            WHERE branch_code = ?
            """;
        
        int rowsAffected = jdbcTemplate.update(updateSql, newName, newManager, threadMarker + "_UPDATED", lifecycleBranchCode);
        
        // Then
        assertEquals(1, rowsAffected);
        
        // Verify the update
        Map<String, Object> updatedData = jdbcTemplate.queryForMap(selectSql, lifecycleBranchCode);
        
        assertEquals(newName, updatedData.get("branch_name"));
        assertEquals(newManager, updatedData.get("manager_name"));
        assertEquals(threadMarker + "_UPDATED", updatedData.get("updated_by"));
        
        // Log lifecycle verification
        log.info("shouldUpdateBranchSuccessfully: Successfully updated branch {} in schema {} (will be cleaned up in @AfterEach)", 
                lifecycleBranchCode, schemaName);
    }
    
    @Test
    void shouldDeleteBranchSuccessfully() {
        // Given - Lifecycle data already prepared in @BeforeEach
        // Verify the initial data exists
        String selectSql = "SELECT * FROM branches WHERE branch_code = ?";
        Map<String, Object> initialData = jdbcTemplate.queryForMap(selectSql, lifecycleBranchCode);
        assertEquals(lifecycleBranchCode, initialData.get("branch_code"));
        assertEquals(lifecycleBranchName, initialData.get("branch_name"));
        assertEquals(threadMarker, initialData.get("created_by"));
        
        int countBefore = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branches WHERE branch_code = ?", 
            Integer.class, lifecycleBranchCode
        );
        assertEquals(1, countBefore);
        
        // When - Delete the branch
        String deleteSql = "DELETE FROM branches WHERE branch_code = ?";
        int rowsAffected = jdbcTemplate.update(deleteSql, lifecycleBranchCode);
        
        // Then
        assertEquals(1, rowsAffected);
        
        // Verify deletion
        int countAfter = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branches WHERE branch_code = ?", 
            Integer.class, lifecycleBranchCode
        );
        assertEquals(0, countAfter);
        
        // Log lifecycle verification
        log.info("shouldDeleteBranchSuccessfully: Successfully deleted branch {} from schema {} (@AfterEach will have nothing to clean)", 
                lifecycleBranchCode, schemaName);
    }
    
    @Test
    void shouldListAllBranches() {
        // This test verifies that Flyway migration data is loaded correctly in the target schema
        
        // When - Query only migration branches (created by 'SYSTEM') to verify Flyway migration data
        String sql = "SELECT * FROM branches WHERE created_by = 'SYSTEM' ORDER BY branch_code";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        
        // Then - Verify Flyway seed data is present (5 branches from V002__insert_initial_data.sql)
        assertNotNull(results);
        assertEquals(5, results.size(), "Expected 5 branches from Flyway migration seed data");
        
        // Verify specific migration data is present and targeting the correct schema
        String[] expectedBranchCodes = {"BDG01", "HO001", "JKT01", "SBY01", "YGY01"};
        String[] expectedBranchNames = {
            "Cabang Bandung", "Kantor Pusat Jakarta", "Cabang Jakarta Timur", 
            "Cabang Surabaya", "Cabang Yogyakarta"
        };
        
        for (int i = 0; i < expectedBranchCodes.length; i++) {
            final String expectedCode = expectedBranchCodes[i];
            final String expectedName = expectedBranchNames[i];
            
            boolean foundBranch = results.stream().anyMatch(row -> 
                expectedCode.equals(row.get("branch_code")) && 
                expectedName.equals(row.get("branch_name"))
            );
            
            assertTrue(foundBranch, "Expected branch " + expectedCode + " (" + expectedName + 
                      ") not found in migration data for schema " + schemaName);
        }
        
        // Verify main branch is correctly identified
        boolean foundMainBranch = results.stream().anyMatch(row -> 
            "HO001".equals(row.get("branch_code")) && 
            Boolean.TRUE.equals(row.get("is_main_branch"))
        );
        assertTrue(foundMainBranch, "Main branch (HO001) should be marked as is_main_branch=true");
        
        // Verify created_by is set correctly from migration
        boolean foundSystemCreated = results.stream().anyMatch(row -> 
            "SYSTEM".equals(row.get("created_by"))
        );
        assertTrue(foundSystemCreated, "Migration data should have created_by='SYSTEM'");
        
        // Log migration verification
        log.info("shouldListAllBranches: Successfully verified Flyway migration data in schema {} - found all 5 expected branches created by SYSTEM", 
                schemaName);
    }
    
    @Test
    void shouldHandleNonExistentBranchGracefully() {
        // Given - Use a branch code that definitely doesn't exist in migration data
        String nonExistentCode = "FAKE999";
        
        // Verify migration data exists first to ensure schema is properly set up
        int migrationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM branches WHERE created_by = 'SYSTEM'", Integer.class);
        assertEquals(5, migrationCount, "Should have 5 migration branches in schema " + schemaName);
        
        // When & Then
        assertThrows(EmptyResultDataAccessException.class, () -> {
            jdbcTemplate.queryForMap("SELECT * FROM branches WHERE branch_code = ?", nonExistentCode);
        });
        
        log.info("shouldHandleNonExistentBranchGracefully: Verified migration data exists and non-existent code {} properly throws exception in schema {}", 
                nonExistentCode, schemaName);
    }
    
    @Test
    void shouldEnforceUniqueConstraintOnBranchCode() {
        // Given - Try to insert a branch with the same code as migration data (HO001)
        String duplicateBranchCode = "HO001";  // Already exists in migration data
        String branchName = TestDataFactory.generateBranchName() + " Duplicate";
        
        // Verify the migration branch exists first
        int existingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branches WHERE branch_code = ?", 
            Integer.class, duplicateBranchCode
        );
        assertEquals(1, existingCount, "Migration data should contain " + duplicateBranchCode);
        
        // When & Then - Try to insert duplicate branch code should fail
        assertThrows(Exception.class, () -> {
            jdbcTemplate.update(
                "INSERT INTO branches (branch_code, branch_name, created_by) VALUES (?, ?, 'TEST_USER')",
                duplicateBranchCode, branchName
            );
        });
        
        log.info("shouldEnforceUniqueConstraintOnBranchCode: Verified unique constraint prevents duplicating migration data branch code {} in schema {}", 
                duplicateBranchCode, schemaName);
    }
    
    @Test
    void shouldVerifySchemaIsolation() {
        // This test verifies that each test is running in its own schema
        
        // Given
        String testMarker = "SCHEMA_TEST_" + java.util.concurrent.ThreadLocalRandom.current().nextInt(10000000, 99999999);
        
        // When - Insert a marker specific to this test thread/schema
        jdbcTemplate.update(
            "INSERT INTO branches (branch_code, branch_name, created_by) VALUES (?, ?, ?)",
            testMarker, "Schema Isolation Test", threadMarker
        );
        
        // Then - Verify the marker exists and schema name is unique
        int count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM branches WHERE branch_code = ?",
            Integer.class, testMarker
        );
        assertEquals(1, count);
        
        // Log the schema name for verification during parallel execution
        log.info("Test running in schema: {} with thread: {} and marker: {}", 
                schemaName, Thread.currentThread().getName(), testMarker);
        
        assertNotNull(schemaName);
        assertTrue(schemaName.startsWith("test_"));
    }
}