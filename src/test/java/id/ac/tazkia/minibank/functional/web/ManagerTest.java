package id.ac.tazkia.minibank.functional.web;

/**
 * Base class for tests requiring Manager permissions.
 * Automatically logs in as manager.
 */
public abstract class ManagerTest extends BaseSeleniumTest {
    
    @Override
    protected void performInitialLogin() {
        getLoginHelper().loginAsManager();
    }
}