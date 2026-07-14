package com.beta.loyalty.repository.receipt;

import com.beta.loyalty.domain.Receipt;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("select r from Receipt r where r.venue.id = :venueId and r.tenantId = :tenantId order by r.issuedAt desc")
    Page<Receipt> findByVenueIdAndTenantId(
            @Param("venueId") UUID venueId,
            @Param("tenantId") UUID tenantId,
            Pageable pageable
    );

    @Query("select r from Receipt r where r.id = :id and r.tenantId = :tenantId")
    Optional<Receipt> findByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);
}
