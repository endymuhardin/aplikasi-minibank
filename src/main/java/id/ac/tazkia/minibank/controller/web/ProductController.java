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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {
    
    private static final String REDIRECT_PRODUCT_LIST = "redirect:/product/list";
    private static final String PRODUCT_TYPES_ATTR = "productTypes";
    private static final String PRODUCT_ATTR = "product";
    private static final String PROFIT_SHARING_TYPES_ATTR = "profitSharingTypes";
    private static final String PROFIT_DISTRIBUTION_FREQ_ATTR = "profitDistributionFrequencies";
    private static final String PRODUCT_FORM_VIEW = "product/form";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String PRODUCT_NOT_FOUND_MSG = "Product not found";
    
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
        model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
        model.addAttribute("categories", productService.findDistinctCategories());
        
        return "product/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute(PRODUCT_ATTR, new Product());
        model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
        model.addAttribute(PROFIT_SHARING_TYPES_ATTR, Product.ProfitSharingType.values());
        model.addAttribute(PROFIT_DISTRIBUTION_FREQ_ATTR, Product.ProfitDistributionFrequency.values());
        return PRODUCT_FORM_VIEW;
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
            model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
            model.addAttribute(PROFIT_SHARING_TYPES_ATTR, Product.ProfitSharingType.values());
            model.addAttribute(PROFIT_DISTRIBUTION_FREQ_ATTR, Product.ProfitDistributionFrequency.values());
            return PRODUCT_FORM_VIEW;
        }
        
        try {
            productService.save(product);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Product created successfully");
            return REDIRECT_PRODUCT_LIST;
        } catch (Exception e) {
            log.error("Error creating product", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error creating product: " + e.getMessage());
            model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
            model.addAttribute(PROFIT_SHARING_TYPES_ATTR, Product.ProfitSharingType.values());
            model.addAttribute(PROFIT_DISTRIBUTION_FREQ_ATTR, Product.ProfitDistributionFrequency.values());
            return PRODUCT_FORM_VIEW;
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PRODUCT_NOT_FOUND_MSG);
            return REDIRECT_PRODUCT_LIST;
        }
        
        model.addAttribute(PRODUCT_ATTR, product.get());
        model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
        model.addAttribute(PROFIT_SHARING_TYPES_ATTR, Product.ProfitSharingType.values());
        model.addAttribute(PROFIT_DISTRIBUTION_FREQ_ATTR, Product.ProfitDistributionFrequency.values());
        return PRODUCT_FORM_VIEW;
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
            model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
            model.addAttribute(PROFIT_SHARING_TYPES_ATTR, Product.ProfitSharingType.values());
            model.addAttribute(PROFIT_DISTRIBUTION_FREQ_ATTR, Product.ProfitDistributionFrequency.values());
            return PRODUCT_FORM_VIEW;
        }
        
        try {
            product.setId(id); // Ensure ID is set
            productService.update(product);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Product updated successfully");
            return REDIRECT_PRODUCT_LIST;
        } catch (Exception e) {
            log.error("Error updating product", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error updating product: " + e.getMessage());
            model.addAttribute(PRODUCT_TYPES_ATTR, Product.ProductType.values());
            model.addAttribute(PROFIT_SHARING_TYPES_ATTR, Product.ProfitSharingType.values());
            model.addAttribute(PROFIT_DISTRIBUTION_FREQ_ATTR, Product.ProfitDistributionFrequency.values());
            return PRODUCT_FORM_VIEW;
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);
        if (product.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PRODUCT_NOT_FOUND_MSG);
            return REDIRECT_PRODUCT_LIST;
        }
        
        model.addAttribute(PRODUCT_ATTR, product.get());
        return "product/view";
    }
    
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            productService.softDelete(id);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Product deactivated successfully");
        } catch (Exception e) {
            log.error("Error deactivating product", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error deactivating product: " + e.getMessage());
        }
        return REDIRECT_PRODUCT_LIST;
    }
    
    @PostMapping("/activate/{id}")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.findById(id);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setIsActive(true);
                productService.update(product);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Product activated successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, PRODUCT_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error activating product", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error activating product: " + e.getMessage());
        }
        return REDIRECT_PRODUCT_LIST;
    }
}
