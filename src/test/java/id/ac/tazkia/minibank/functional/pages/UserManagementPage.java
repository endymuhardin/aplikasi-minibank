package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserManagementPage {
    
    private final Page page;
    
    public UserManagementPage(Page page) {
        this.page = page;
    }
    
    public boolean isLoaded() {
        try {
            return page.url().contains("/user");
        } catch (Exception e) {
            return false;
        }
    }
}