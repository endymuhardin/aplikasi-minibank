package id.ac.tazkia.minibank.controller.rest;

import id.ac.tazkia.minibank.entity.User;
import id.ac.tazkia.minibank.entity.UserPassword;
import id.ac.tazkia.minibank.entity.Branch;
import id.ac.tazkia.minibank.repository.UserRepository;
import id.ac.tazkia.minibank.repository.UserPasswordRepository;
import id.ac.tazkia.minibank.repository.BranchRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    
    
    private final UserRepository userRepository;
    private final UserPasswordRepository userPasswordRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserRestController(UserRepository userRepository, 
                             UserPasswordRepository userPasswordRepository,
                             BranchRepository branchRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userPasswordRepository = userPasswordRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<Object> createUser(@Valid @RequestBody CreateUserRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        // Check for username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("username", "Username already exists");
            return ResponseEntity.badRequest().body(errors);
        }

        // Check for email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "Email already exists");
            return ResponseEntity.badRequest().body(errors);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        // AuditorAware will automatically set createdBy and updatedBy
        
        // Handle branch assignment
        if (request.getBranch() != null && request.getBranch().getId() != null) {
            Branch branch = branchRepository.findById(request.getBranch().getId())
                .orElse(null);
            if (branch == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("branch", "Branch not found with id: " + request.getBranch().getId());
                return ResponseEntity.badRequest().body(errors);
            }
            user.setBranch(branch);
        }
        
        User savedUser = userRepository.save(user);

        // Create password
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            UserPassword userPassword = new UserPassword();
            userPassword.setUser(savedUser);
            userPassword.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            // AuditorAware will automatically set createdBy
            userPasswordRepository.save(userPassword);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(savedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElse(ResponseEntity.<Object>notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(@RequestParam(required = false) String search) {
        List<User> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findUsersWithSearchTerm(search.trim());
        } else {
            users = userRepository.findAll();
        }
        
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::new)
                .toList();
        
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/active")
    public ResponseEntity<List<UserResponse>> getActiveUsers() {
        List<User> users = userRepository.findActiveUsers();
        List<UserResponse> userResponses = users.stream()
                .map(UserResponse::new)
                .toList();
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElse(ResponseEntity.<Object>notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        return userRepository.findById(id)
                .map(user -> {
                    // Check for username uniqueness (excluding current user)
                    if (!user.getUsername().equals(request.getUsername()) && 
                        userRepository.existsByUsername(request.getUsername())) {
                        Map<String, String> errors = new HashMap<>();
                        errors.put("username", "Username already exists");
                        return ResponseEntity.badRequest().body(errors);
                    }

                    // Check for email uniqueness (excluding current user)
                    if (!user.getEmail().equals(request.getEmail()) && 
                        userRepository.existsByEmail(request.getEmail())) {
                        Map<String, String> errors = new HashMap<>();
                        errors.put("email", "Email already exists");
                        return ResponseEntity.badRequest().body(errors);
                    }

                    user.setUsername(request.getUsername());
                    user.setEmail(request.getEmail());
                    user.setFullName(request.getFullName());
                    // AuditorAware will automatically set updatedBy
                    
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(new UserResponse(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setIsActive(true);
                    // AuditorAware will automatically set updatedBy
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(new UserResponse(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setIsActive(false);
                    // AuditorAware will automatically set updatedBy
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(new UserResponse(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<UserResponse> unlockUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.resetFailedLoginAttempts();
                    user.setIsLocked(false);
                    // AuditorAware will automatically set updatedBy
                    User updatedUser = userRepository.save(user);
                    return ResponseEntity.ok(new UserResponse(updatedUser));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, String>> changePassword(@PathVariable UUID id, @Valid @RequestBody ChangePasswordRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> 
                errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(errors);
        }

        return userRepository.findById(id)
                .map(user -> {
                    // Deactivate old password
                    userPasswordRepository.findByUser(user)
                            .ifPresent(oldPassword -> {
                                oldPassword.setExpired();
                                userPasswordRepository.save(oldPassword);
                            });

                    // Create new password
                    UserPassword userPassword = new UserPassword();
                    userPassword.setUser(user);
                    userPassword.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                    // AuditorAware will automatically set createdBy
                    userPasswordRepository.save(userPassword);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Password changed successfully");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.<Object>notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "User deleted successfully");
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.<Object>notFound().build());
    }


    // DTOs
    public static class CreateUserRequest {
        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        @jakarta.validation.constraints.Size(max = 50, message = "Username must not exceed 50 characters")
        private String username;
        
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Email should be valid")
        @jakarta.validation.constraints.Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;
        
        @jakarta.validation.constraints.NotBlank(message = "Full name is required")
        @jakarta.validation.constraints.Size(max = 100, message = "Full name must not exceed 100 characters")
        private String fullName;
        
        @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
        
        private BranchRef branch;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public BranchRef getBranch() { return branch; }
        public void setBranch(BranchRef branch) { this.branch = branch; }
    }

    public static class UpdateUserRequest {
        @jakarta.validation.constraints.NotBlank(message = "Username is required")
        @jakarta.validation.constraints.Size(max = 50, message = "Username must not exceed 50 characters")
        private String username;
        
        @jakarta.validation.constraints.NotBlank(message = "Email is required")
        @jakarta.validation.constraints.Email(message = "Email should be valid")
        @jakarta.validation.constraints.Size(max = 100, message = "Email must not exceed 100 characters")
        private String email;
        
        @jakarta.validation.constraints.NotBlank(message = "Full name is required")
        @jakarta.validation.constraints.Size(max = 100, message = "Full name must not exceed 100 characters")
        private String fullName;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
    }

    public static class ChangePasswordRequest {
        @jakarta.validation.constraints.NotBlank(message = "New password is required")
        @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;

        // Getters and setters
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class UserResponse {
        private UUID id;
        private String username;
        private String email;
        private String fullName;
        private Boolean isActive;
        private Boolean isLocked;
        private LocalDateTime lastLogin;
        private Integer failedLoginAttempts;
        private LocalDateTime lockedUntil;
        private LocalDateTime createdDate;
        private LocalDateTime updatedDate;

        public UserResponse(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.isActive = user.getIsActive();
            this.isLocked = user.getIsLocked();
            this.lastLogin = user.getLastLogin();
            this.failedLoginAttempts = user.getFailedLoginAttempts();
            this.lockedUntil = user.getLockedUntil();
            this.createdDate = user.getCreatedDate();
            this.updatedDate = user.getUpdatedDate();
        }

        // Getters and setters
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        public Boolean getIsLocked() { return isLocked; }
        public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }
        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
        public Integer getFailedLoginAttempts() { return failedLoginAttempts; }
        public void setFailedLoginAttempts(Integer failedLoginAttempts) { this.failedLoginAttempts = failedLoginAttempts; }
        public LocalDateTime getLockedUntil() { return lockedUntil; }
        public void setLockedUntil(LocalDateTime lockedUntil) { this.lockedUntil = lockedUntil; }
        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
        public LocalDateTime getUpdatedDate() { return updatedDate; }
        public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    }
    
    public static class BranchRef {
        private UUID id;
        
        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
    }
}