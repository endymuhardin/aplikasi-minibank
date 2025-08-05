package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sequence_numbers")
@Data
@NoArgsConstructor
public class SequenceNumber {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "sequence_name", unique = true, nullable = false, length = 50)
    private String sequenceName;
    
    @Column(name = "last_number", nullable = false)
    private Long lastNumber = 0L;
    
    @Column(name = "prefix", length = 10)
    private String prefix;
    
    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    // Business methods
    public synchronized Long getNextNumber() {
        this.lastNumber++;
        return this.lastNumber;
    }
    
    public String generateNextSequence() {
        Long nextNumber = getNextNumber();
        if (prefix != null && !prefix.isEmpty()) {
            return prefix + String.format("%07d", nextNumber);
        }
        return String.format("%07d", nextNumber);
    }
    
    public void resetSequence(Long startNumber) {
        this.lastNumber = startNumber;
    }
}