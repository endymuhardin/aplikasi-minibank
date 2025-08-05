package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.SequenceNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SequenceNumberRepository extends JpaRepository<SequenceNumber, UUID> {
    
    Optional<SequenceNumber> findBySequenceName(String sequenceName);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SequenceNumber s WHERE s.sequenceName = :sequenceName")
    Optional<SequenceNumber> findBySequenceNameWithLock(@Param("sequenceName") String sequenceName);
    
    @Modifying
    @Query("UPDATE SequenceNumber s SET s.lastNumber = s.lastNumber + 1 WHERE s.sequenceName = :sequenceName")
    int incrementSequenceNumber(@Param("sequenceName") String sequenceName);
    
    @Modifying
    @Query("UPDATE SequenceNumber s SET s.lastNumber = :newValue WHERE s.sequenceName = :sequenceName")
    int resetSequenceNumber(@Param("sequenceName") String sequenceName, @Param("newValue") Long newValue);
    
    @Query("SELECT s.lastNumber FROM SequenceNumber s WHERE s.sequenceName = :sequenceName")
    Optional<Long> getCurrentSequenceValue(@Param("sequenceName") String sequenceName);
    
    boolean existsBySequenceName(String sequenceName);
}