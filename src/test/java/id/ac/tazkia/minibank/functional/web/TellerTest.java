package id.ac.tazkia.minibank.functional.web;

/**
 * Base class for tests requiring Teller permissions.
 * Automatically logs in as teller.
 */
public abstract class TellerTest extends BaseSeleniumTest {
    
    @Override
    protected void performInitialLogin() {
        getLoginHelper().loginAsTeller();
    }
}