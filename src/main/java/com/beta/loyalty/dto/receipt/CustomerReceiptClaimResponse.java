package com.beta.loyalty.dto.receipt;

import com.beta.loyalty.domain.ReceiptClaim;
import com.beta.loyalty.domain.enums.ReceiptClaimStatus;
import com.beta.loyalty.domain.enums.ReceiptStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerReceiptClaimResponse(
        UUID claimId,
        UUID receiptId,
        UUID venueId,
        String venueName,
        BigDecimal amount,
        String currency,
        OffsetDateTime issuedAt,
        ReceiptStatus receiptStatus,
        ReceiptClaimStatus claimStatus,
        long pointsEarned,
        OffsetDateTime claimedAt
) {
    public static CustomerReceiptClaimResponse from(ReceiptClaim c) {
        return new CustomerReceiptClaimResponse(
                c.getId(),
                c.getReceipt().getId(),
                c.getVenue().getId(),
                c.getVenue().getName(),
                c.getReceipt().getAmount(),
                c.getReceipt().getCurrency(),
                c.getReceipt().getIssuedAt(),
                c.getReceipt().getStatus(),
                c.getStatus(),
                c.getCalculatedPointsTotal(),
                c.getCreatedAt()
        );
    }
}
