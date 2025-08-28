package id.ac.tazkia.minibank.playwright.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AccountManagementPage {
    
    private final Page page;
    
    public AccountManagementPage(Page page) {
        this.page = page;
    }
    
    public boolean isLoaded() {
        try {
            return page.url().contains("/account");
        } catch (Exception e) {
            return false;
        }
    }
}