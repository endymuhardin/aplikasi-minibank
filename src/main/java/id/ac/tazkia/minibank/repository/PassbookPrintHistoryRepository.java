package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Passbook;
import id.ac.tazkia.minibank.entity.PassbookPrintHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PassbookPrintHistoryRepository extends JpaRepository<PassbookPrintHistory, UUID> {

    List<PassbookPrintHistory> findByPassbook(Passbook passbook);

    List<PassbookPrintHistory> findByPassbookId(UUID passbookId);

    Page<PassbookPrintHistory> findByPassbookIdOrderByPrintDateDesc(UUID passbookId, Pageable pageable);

    @Query("SELECT h FROM PassbookPrintHistory h WHERE h.passbook.id = :passbookId " +
           "ORDER BY h.printDate DESC LIMIT 1")
    Optional<PassbookPrintHistory> findLastPrintByPassbookId(@Param("passbookId") UUID passbookId);

    @Query("SELECT h FROM PassbookPrintHistory h WHERE h.passbook.id = :passbookId " +
           "AND h.printDate BETWEEN :startDate AND :endDate ORDER BY h.printDate DESC")
    List<PassbookPrintHistory> findByPassbookIdAndDateRange(@Param("passbookId") UUID passbookId,
                                                           @Param("startDate") LocalDateTime startDate,
                                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(h) FROM PassbookPrintHistory h WHERE h.passbook.id = :passbookId")
    Long countByPassbookId(@Param("passbookId") UUID passbookId);

    List<PassbookPrintHistory> findByStatus(PassbookPrintHistory.PrintStatus status);
}
