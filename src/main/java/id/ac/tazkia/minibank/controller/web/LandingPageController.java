package id.ac.tazkia.minibank.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {

    // Tampil saat buka http://localhost:8080 dan juga /welcome
    @GetMapping({"/", "/welcome"})
    public String landingPage() {
        // mengarah ke templates/landing/index.html
        return "landing/index";
    }
}
