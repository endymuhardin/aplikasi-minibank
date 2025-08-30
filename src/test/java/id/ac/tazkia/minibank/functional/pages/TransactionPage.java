package id.ac.tazkia.minibank.functional.pages;

import com.microsoft.playwright.Page;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionPage {
    
    private final Page page;
    
    public TransactionPage(Page page) {
        this.page = page;
    }
    
    public boolean isLoaded() {
        try {
            return page.url().contains("/transaction");
        } catch (Exception e) {
            return false;
        }
    }
}