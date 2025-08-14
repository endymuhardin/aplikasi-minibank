package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@ToString(exclude = {"rolePermissions"})
@NoArgsConstructor
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotBlank(message = "Permission code is required")
    @Size(max = 100, message = "Permission code must not exceed 100 characters")
    @Column(name = "permission_code", unique = true, nullable = false, length = 100)
    private String permissionCode;
    
    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name must not exceed 100 characters")
    @Column(name = "permission_name", nullable = false, length = 100)
    private String permissionName;
    
    @NotBlank(message = "Permission category is required")
    @Size(max = 50, message = "Permission category must not exceed 50 characters")
    @Column(name = "permission_category", nullable = false, length = 50)
    private String permissionCategory;
    
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // Audit fields
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    // Relationships
    @OneToMany(mappedBy = "permission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<RolePermission> rolePermissions;
}