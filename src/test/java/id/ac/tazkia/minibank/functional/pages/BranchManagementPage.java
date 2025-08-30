package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BranchManagementPage {
    
    private final Page page;
    
    public BranchManagementPage(Page page) {
        this.page = page;
    }
    
    public boolean isLoaded() {
        try {
            return page.url().contains("/branch");
        } catch (Exception e) {
            return false;
        }
    }
}