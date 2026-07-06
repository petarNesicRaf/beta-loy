package com.beta.loyalty.dto.receipt;

import java.util.UUID;

public record ReceiptClaimResponse(
        UUID receiptId,
        UUID claimId,
        String status,
        long pointsEarned
) {
}
