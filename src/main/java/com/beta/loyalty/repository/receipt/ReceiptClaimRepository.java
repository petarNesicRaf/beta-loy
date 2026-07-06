package com.beta.loyalty.repository.receipt;

import com.beta.loyalty.domain.ReceiptClaim;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReceiptClaimRepository extends JpaRepository<ReceiptClaim, UUID> {
    Optional<ReceiptClaim> findByReceipt_Id(UUID receiptId);
    boolean existsByReceipt_Id(UUID receiptId);
}
