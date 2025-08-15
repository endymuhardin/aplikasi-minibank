package id.ac.tazkia.minibank.controller.web;

import id.ac.tazkia.minibank.dto.PersonalAccountOpeningRequest;
import id.ac.tazkia.minibank.entity.IdentityType;
import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.repository.ProductRepository;
import id.ac.tazkia.minibank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account/open")
public class AccountOpeningWebController {

    private final AccountService accountService;
    private final ProductRepository productRepository;

    public AccountOpeningWebController(AccountService accountService, ProductRepository productRepository) {
        this.accountService = accountService;
        this.productRepository = productRepository;
    }

    @ModelAttribute("products")
    public List<Product> getActiveProducts() {
        return productRepository.findByIsActiveTrue();
    }

    @ModelAttribute("identityTypes")
    public IdentityType[] getIdentityTypes() {
        return IdentityType.values();
    }

    @GetMapping("/personal")
    public String showPersonalAccountOpeningForm(Model model) {
        if (!model.containsAttribute("personalAccountOpeningRequest")) {
            model.addAttribute("personalAccountOpeningRequest", new PersonalAccountOpeningRequest());
        }
        return "account/form";
    }

    @PostMapping("/personal")
    public String processPersonalAccountOpeningForm(@Valid @ModelAttribute("personalAccountOpeningRequest") PersonalAccountOpeningRequest request,
                                                    BindingResult bindingResult,
                                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.personalAccountOpeningRequest", bindingResult);
            redirectAttributes.addFlashAttribute("personalAccountOpeningRequest", request);
            return "redirect:/account/open/personal";
        }

        try {
            // In a real app, the user would be from the security context
            request.setCreatedBy("teller1");
            accountService.openPersonalAccount(request);
            redirectAttributes.addFlashAttribute("successMessage", "Personal account opened successfully!");
            return "redirect:/account/open/personal";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("personalAccountOpeningRequest", request);
            return "redirect:/account/open/personal";
        }
    }
}
