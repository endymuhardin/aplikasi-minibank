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

import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.repository.BranchRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/branch")
@RequiredArgsConstructor
public class BranchController {
    
    private static final String REDIRECT_BRANCH_LIST = "redirect:/branch/list";
    private static final String BRANCH_STATUSES_ATTR = "branchStatuses";
    private static final String BRANCH_ATTR = "branch";
    private static final String BRANCH_FORM_VIEW = "branch/form";
    private static final String SUCCESS_MESSAGE_ATTR = "successMessage";
    private static final String ERROR_MESSAGE_ATTR = "errorMessage";
    private static final String BRANCH_NOT_FOUND_MSG = "Branch not found";
    
    private final BranchRepository branchRepository;
    
    @GetMapping("/list")
    public String branchList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "branchCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Branch.BranchStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String search,
            Model model) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Branch> branches;
        if (status != null) {
            branches = branchRepository.findByStatus(status, pageable);
        } else if (city != null && !city.trim().isEmpty()) {
            branches = branchRepository.findByCityContainingIgnoreCase(city.trim(), pageable);
        } else if (search != null && !search.trim().isEmpty()) {
            branches = branchRepository.findByBranchCodeContainingIgnoreCaseOrBranchNameContainingIgnoreCaseOrCityContainingIgnoreCase(
                search.trim(), search.trim(), search.trim(), pageable);
        } else {
            branches = branchRepository.findAll(pageable);
        }
        
        model.addAttribute("branches", branches);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", branches.getTotalPages());
        model.addAttribute("totalItems", branches.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("city", city);
        model.addAttribute("search", search);
        
        // Add filter options
        model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
        
        return "branch/list";
    }
    
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute(BRANCH_ATTR, new Branch());
        model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
        return BRANCH_FORM_VIEW;
    }
    
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute Branch branch, 
                        BindingResult result, 
                        Model model,
                        RedirectAttributes redirectAttributes) {
        
        if (branchRepository.existsByBranchCode(branch.getBranchCode())) {
            result.rejectValue("branchCode", "error.branch", "Branch code already exists");
        }
        
        // Ensure only one main branch exists
        if (Boolean.TRUE.equals(branch.getIsMainBranch())) {
            Optional<Branch> existingMainBranch = branchRepository.findByIsMainBranchTrue();
            if (existingMainBranch.isPresent()) {
                result.rejectValue("isMainBranch", "error.branch", "Only one main branch is allowed");
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
            return BRANCH_FORM_VIEW;
        }
        
        try {
            // AuditorAware will automatically set createdBy
            branchRepository.save(branch);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Branch created successfully");
            return REDIRECT_BRANCH_LIST;
        } catch (Exception e) {
            log.error("Error creating branch", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error creating branch: " + e.getMessage());
            model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
            return BRANCH_FORM_VIEW;
        }
    }
    
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Branch> branch = branchRepository.findById(id);
        if (branch.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, BRANCH_NOT_FOUND_MSG);
            return REDIRECT_BRANCH_LIST;
        }
        
        model.addAttribute(BRANCH_ATTR, branch.get());
        model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
        return BRANCH_FORM_VIEW;
    }
    
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable UUID id,
                      @Valid @ModelAttribute Branch branch, 
                      BindingResult result, 
                      Model model,
                      RedirectAttributes redirectAttributes) {
        
        // Check if branch code exists for different branch
        Optional<Branch> existingBranch = branchRepository.findByBranchCode(branch.getBranchCode());
        if (existingBranch.isPresent() && !existingBranch.get().getId().equals(id)) {
            result.rejectValue("branchCode", "error.branch", "Branch code already exists");
        }
        
        // Ensure only one main branch exists
        if (Boolean.TRUE.equals(branch.getIsMainBranch())) {
            Optional<Branch> existingMainBranch = branchRepository.findByIsMainBranchTrue();
            if (existingMainBranch.isPresent() && !existingMainBranch.get().getId().equals(id)) {
                result.rejectValue("isMainBranch", "error.branch", "Only one main branch is allowed");
            }
        }
        
        if (result.hasErrors()) {
            model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
            return BRANCH_FORM_VIEW;
        }
        
        try {
            branch.setId(id); // Ensure ID is set
            // AuditorAware will automatically set updatedBy
            branchRepository.save(branch);
            redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Branch updated successfully");
            return REDIRECT_BRANCH_LIST;
        } catch (Exception e) {
            log.error("Error updating branch", e);
            model.addAttribute(ERROR_MESSAGE_ATTR, "Error updating branch: " + e.getMessage());
            model.addAttribute(BRANCH_STATUSES_ATTR, Branch.BranchStatus.values());
            return BRANCH_FORM_VIEW;
        }
    }
    
    @GetMapping("/view/{id}")
    public String view(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Branch> branch = branchRepository.findById(id);
        if (branch.isEmpty()) {
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, BRANCH_NOT_FOUND_MSG);
            return REDIRECT_BRANCH_LIST;
        }
        
        model.addAttribute(BRANCH_ATTR, branch.get());
        return "branch/view";
    }
    
    @PostMapping("/deactivate/{id}")
    public String deactivate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Branch> branchOpt = branchRepository.findById(id);
            if (branchOpt.isPresent()) {
                Branch branch = branchOpt.get();
                
                // Prevent deactivating main branch
                if (Boolean.TRUE.equals(branch.getIsMainBranch())) {
                    redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Cannot deactivate main branch");
                    return REDIRECT_BRANCH_LIST;
                }
                
                branch.setStatus(Branch.BranchStatus.INACTIVE);
                // AuditorAware will automatically set updatedBy
                branchRepository.save(branch);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Branch deactivated successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, BRANCH_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error deactivating branch", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error deactivating branch: " + e.getMessage());
        }
        return REDIRECT_BRANCH_LIST;
    }
    
    @PostMapping("/activate/{id}")
    public String activate(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Branch> branchOpt = branchRepository.findById(id);
            if (branchOpt.isPresent()) {
                Branch branch = branchOpt.get();
                branch.setStatus(Branch.BranchStatus.ACTIVE);
                // AuditorAware will automatically set updatedBy
                branchRepository.save(branch);
                redirectAttributes.addFlashAttribute(SUCCESS_MESSAGE_ATTR, "Branch activated successfully");
            } else {
                redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, BRANCH_NOT_FOUND_MSG);
            }
        } catch (Exception e) {
            log.error("Error activating branch", e);
            redirectAttributes.addFlashAttribute(ERROR_MESSAGE_ATTR, "Error activating branch: " + e.getMessage());
        }
        return REDIRECT_BRANCH_LIST;
    }
}