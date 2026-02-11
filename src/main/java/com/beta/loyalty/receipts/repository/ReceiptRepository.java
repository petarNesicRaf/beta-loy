package com.beta.loyalty.receipts.repository;

import com.beta.loyalty.domain.receipt.Receipt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptRepository extends JpaRepository<Receipt, UUID> {
    Optional<Receipt> findByVenue_IdAndReceiptHash(UUID venueId, String receiptHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select r from Receipt r
        where r.venue.id = :venueId and r.receiptHash = :hash
    """)
    Optional<Receipt> findForUpdate(@Param("venueId") UUID venueId, @Param("hash") String hash);

}
