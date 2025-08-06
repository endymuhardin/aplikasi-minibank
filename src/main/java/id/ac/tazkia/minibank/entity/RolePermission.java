package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "role_permissions")
@Data
@NoArgsConstructor
public class RolePermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_roles", nullable = false)
    private Role role;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_permissions", nullable = false)
    private Permission permission;
    
    @CreationTimestamp
    @Column(name = "granted_date", updatable = false)
    private LocalDateTime grantedDate;
    
    @Column(name = "granted_by", length = 100)
    private String grantedBy;
}