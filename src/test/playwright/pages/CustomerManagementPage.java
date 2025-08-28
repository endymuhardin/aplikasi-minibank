package id.ac.tazkia.minibank.playwright.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomerManagementPage {
    
    private final Page page;
    
    public CustomerManagementPage(Page page) {
        this.page = page;
    }
    
    // Basic implementation - can be extended as needed
    public boolean isLoaded() {
        try {
            return page.url().contains("/customer");
        } catch (Exception e) {
            return false;
        }
    }
}