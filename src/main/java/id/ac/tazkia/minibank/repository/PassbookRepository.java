package id.ac.tazkia.minibank.repository;

import id.ac.tazkia.minibank.entity.Account;
import id.ac.tazkia.minibank.entity.Passbook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PassbookRepository extends JpaRepository<Passbook, UUID> {

    Optional<Passbook> findByPassbookNumber(String passbookNumber);

    Optional<Passbook> findByAccount(Account account);

    Optional<Passbook> findByAccountId(UUID accountId);

    List<Passbook> findByStatus(Passbook.PassbookStatus status);

    @Query("SELECT p FROM Passbook p WHERE p.account.id = :accountId AND p.status = 'ACTIVE'")
    Optional<Passbook> findActiveByAccountId(@Param("accountId") UUID accountId);

    @Query("SELECT p FROM Passbook p JOIN FETCH p.account a JOIN FETCH a.customer " +
           "WHERE p.passbookNumber = :passbookNumber")
    Optional<Passbook> findByPassbookNumberWithDetails(@Param("passbookNumber") String passbookNumber);

    @Query("SELECT p FROM Passbook p JOIN FETCH p.account a JOIN FETCH a.customer " +
           "WHERE p.account.id = :accountId AND p.status = 'ACTIVE'")
    Optional<Passbook> findActiveByAccountIdWithDetails(@Param("accountId") UUID accountId);

    boolean existsByAccountId(UUID accountId);

    boolean existsByPassbookNumber(String passbookNumber);
}
