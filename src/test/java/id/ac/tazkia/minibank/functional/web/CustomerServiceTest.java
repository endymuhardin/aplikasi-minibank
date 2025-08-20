package id.ac.tazkia.minibank.functional.web;

/**
 * Base class for tests requiring Customer Service permissions.
 * Automatically logs in as customer service user.
 */
public abstract class CustomerServiceTest extends BaseSeleniumTest {
    
    @Override
    protected void performInitialLogin() {
        getLoginHelper().loginAsCustomerServiceUser();
    }
}