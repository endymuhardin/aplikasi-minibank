package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_roles")
@Getter
@Setter
@ToString(exclude = {"user", "role"})
@NoArgsConstructor
public class UserRole {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_users", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_roles", nullable = false)
    private Role role;
    
    @CreationTimestamp
    @Column(name = "assigned_date", updatable = false)
    private LocalDateTime assignedDate;
    
    @Column(name = "assigned_by", length = 100)
    private String assignedBy;
}