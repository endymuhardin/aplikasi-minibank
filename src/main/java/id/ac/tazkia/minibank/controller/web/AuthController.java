package id.ac.tazkia.minibank.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }
        
        return "auth/login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard/index";
    }
}