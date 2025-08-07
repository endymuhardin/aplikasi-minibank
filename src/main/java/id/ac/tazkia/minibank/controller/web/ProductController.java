package id.ac.tazkia.minibank.controller.web;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import id.ac.tazkia.minibank.entity.Product;
import id.ac.tazkia.minibank.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping("/list")
    public String productList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Product.ProductType productType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products;
        if (productType != null || 
            (category != null && !category.trim().isEmpty()) || 
            (search != null && !search.trim().isEmpty())) {
            products = productService.findWithFilters(productType, category, search, pageable);
        } else {
            products = productService.findAll(pageable);
        }
        
        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("productType", productType);
        model.addAttribute("category", category);
        model.addAttribute("search", search);
        
        // Add filter options
        model.addAttribute("productTypes", Product.ProductType.values());
        model.addAttribute("categories", productService.findDistinctCategories());
        
        return "product/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("productTypes", Product.ProductType.values());
        model.addAttribute("interestCalculationTypes", Product.InterestCalculationType.values());
        model.addAttribute("interestPaymentFrequencies", Product.InterestPaymentFrequency.values());
        return "product/form";
    }
    
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Product product, 
                        BindingResult result, 
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        
        if (productService.existsByProductCode(product.getProductCode())) {
            result.rejectValue("productCode", "error.product", "Product code already exists");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("productTypes", Product.ProductType.values());
            model.addAttribute("interestCalculationTypes", Product.InterestCalculationType.values());
            model.addAttribute("interestPaymentFrequencies", Product.InterestPaymentFrequency.values());
            return "product/form";
        }
        
        try {
            productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product created successfully");
            return "redirect:/product/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating product: " + e.getMessage());
            model.addAttribute("productTypes", Product.ProductType.values());
            model.addAttribute("interestCalculationTypes", Product.InterestCalculationType.values());
            model.addAttribute("interestPaymentFrequencies", Product.InterestPaymentFrequency.values());
            return "product/form";
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
            return "redirect:/product/list";
        }
        
        model.addAttribute("product", product.get());
        model.addAttribute("productTypes", Product.ProductType.values());
        model.addAttribute("interestCalculationTypes", Product.InterestCalculationType.values());
        model.addAttribute("interestPaymentFrequencies", Product.InterestPaymentFrequency.values());
        return "product/form";
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable UUID id,
                      @Valid @ModelAttribute Product product, 
                      BindingResult result, 
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        if (productService.existsByProductCodeAndNotId(product.getProductCode(), id)) {
            result.rejectValue("productCode", "error.product", "Product code already exists");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("productTypes", Product.ProductType.values());
            model.addAttribute("interestCalculationTypes", Product.InterestCalculationType.values());
            model.addAttribute("interestPaymentFrequencies", Product.InterestPaymentFrequency.values());
            return "product/form";
        }
        
        try {
            product.setId(id); // Ensure ID is set
            productService.update(product);
            redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully");
            return "redirect:/product/list";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating product: " + e.getMessage());
            model.addAttribute("productTypes", Product.ProductType.values());
            model.addAttribute("interestCalculationTypes", Product.InterestCalculationType.values());
            model.addAttribute("interestPaymentFrequencies", Product.InterestPaymentFrequency.values());
            return "product/form";
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
            return "redirect:/product/list";
        }
        
        model.addAttribute("product", product.get());
        return "product/view";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            productService.softDelete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Product deactivated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating product: " + e.getMessage());
        }
        return "redirect:/product/list";
    }
    
    @PostMapping("/activate/{id}")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.findById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setIsActive(true);
                productService.update(product);
                redirectAttributes.addFlashAttribute("successMessage", "Product activated successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Product not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error activating product: " + e.getMessage());
        }
        return "redirect:/product/list";
    }
}
