package com.beta.loyalty.receipts.dto;

import java.util.UUID;

public record ReceiptClaimResponse(
        UUID receiptId,
        UUID claimId,
        String status,
        long pointsEarned
) {
}
