package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Base entity class that provides automatic auditing fields.
 * All entities that need audit trail should extend this class.
 * 
 * The audit fields are automatically populated by Spring Data JPA
 * using the AuditorAware implementation.
 */
@MappedSuperclass
@Data
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    private String createdBy;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
}