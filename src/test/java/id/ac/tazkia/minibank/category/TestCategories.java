package id.ac.tazkia.minibank.category;

/**
 * Test categorization system for parallel execution and test organization.
 * Categories are designed to optimize test execution by grouping tests with similar
 * execution characteristics and resource requirements.
 */
public final class TestCategories {
    
    // Execution Speed Categories
    
    /**
     * Fast tests that execute in under 1 second.
     * Typically unit tests, simple repository tests, validation tests.
     * Suitable for frequent execution and CI pipelines.
     */
    public interface FastTests {}
    
    /**
     * Medium speed tests that execute in 1-10 seconds.
     * Integration tests, REST API tests, service layer tests.
     * Good for development feedback loops.
     */
    public interface MediumTests {}
    
    /**
     * Slow tests that execute in 10+ seconds.
     * UI tests, end-to-end tests, complex integration scenarios.
     * Typically run less frequently, in dedicated test phases.
     */
    public interface SlowTests {}
    
    // Functional Categories
    
    /**
     * Pure unit tests with no external dependencies.
     * No database, no network, no file system access.
     * Highly parallelizable and deterministic.
     */
    public interface UnitTests {}
    
    /**
     * Integration tests that use real database connections.
     * Tests repository layer, data access, database constraints.
     * Require database state management and cleanup.
     */
    public interface DatabaseTests {}
    
    /**
     * REST API integration tests.
     * Test HTTP endpoints, request/response handling, authentication.
     * Require application context and network layer.
     */
    public interface ApiTests {}
    
    /**
     * Selenium-based UI tests.
     * Test user interface, browser interactions, end-to-end workflows.
     * Require browser containers and longer execution times.
     */
    public interface UiTests {}
    
    /**
     * Security and authentication tests.
     * Test RBAC, user permissions, authentication flows.
     * May require special user setups and security contexts.
     */
    public interface SecurityTests {}
    
    // Resource Usage Categories
    
    /**
     * Tests that require minimal resources.
     * Can run in parallel with high concurrency.
     * Suitable for resource-constrained environments.
     */
    public interface LightweightTests {}
    
    /**
     * Tests that require significant resources.
     * Should run with limited concurrency to avoid resource contention.
     * May require dedicated execution phases.
     */
    public interface HeavyweightTests {}
    
    /**
     * Tests that require exclusive access to resources.
     * Must run sequentially to avoid conflicts.
     * Examples: global state tests, container lifecycle tests.
     */
    public interface ExclusiveTests {}
    
    // Execution Context Categories
    
    /**
     * Tests that can run in parallel without any coordination.
     * Fully isolated, no shared state, no external dependencies.
     * Ideal for maximum parallelization.
     */
    public interface ParallelSafe {}
    
    /**
     * Tests that require coordination or have shared dependencies.
     * May need sequential execution or careful resource management.
     * Examples: database schema tests, singleton tests.
     */
    public interface SequentialOnly {}
    
    /**
     * Tests that modify global or shared state.
     * Require careful isolation and cleanup.
     * Examples: configuration tests, system property tests.
     */
    public interface StatefulTests {}
    
    // Business Domain Categories
    
    /**
     * Tests related to customer management functionality.
     * Customer registration, validation, CRUD operations.
     */
    public interface CustomerTests {}
    
    /**
     * Tests related to account management functionality.
     * Account opening, closing, balance operations.
     */
    public interface AccountTests {}
    
    /**
     * Tests related to transaction processing.
     * Deposits, withdrawals, transfers, transaction history.
     */
    public interface TransactionTests {}
    
    /**
     * Tests related to Islamic banking products.
     * Mudharabah, Musharakah, Wadiah products and calculations.
     */
    public interface ProductTests {}
    
    /**
     * Tests related to user management and RBAC.
     * User creation, role assignment, permission validation.
     */
    public interface UserManagementTests {}
    
    /**
     * Tests related to reporting and analytics.
     * Balance reports, transaction summaries, audit trails.
     */
    public interface ReportingTests {}
    
    // Test Environment Categories
    
    /**
     * Tests that run in development environment.
     * May include debug features, relaxed validations.
     */
    public interface DevelopmentTests {}
    
    /**
     * Tests suitable for continuous integration.
     * Fast, reliable, essential functionality coverage.
     */
    public interface ContinuousIntegrationTests {}
    
    /**
     * Tests for staging/pre-production environment.
     * Comprehensive testing with production-like data.
     */
    public interface StagingTests {}
    
    /**
     * Tests for production validation.
     * Smoke tests, health checks, critical path validation.
     */
    public interface ProductionTests {}
    
    // Composite Categories for Common Use Cases
    
    /**
     * Fast tests suitable for CI/CD pipelines.
     * Combines FastTests + ParallelSafe + ContinuousIntegrationTests
     */
    public interface CIPipelineTests extends FastTests, ParallelSafe, ContinuousIntegrationTests {}
    
    /**
     * Comprehensive regression tests.
     * Covers all functional areas with medium to slow execution.
     */
    public interface RegressionTests extends MediumTests, SlowTests {}
    
    /**
     * End-to-end workflow tests.
     * Complete business scenarios from UI to database.
     */
    public interface EndToEndTests extends UiTests, DatabaseTests, SlowTests {}
    
    /**
     * Performance and load tests.
     * Resource-intensive tests that measure system performance.
     */
    public interface PerformanceTests extends HeavyweightTests, ExclusiveTests, SlowTests {}
}