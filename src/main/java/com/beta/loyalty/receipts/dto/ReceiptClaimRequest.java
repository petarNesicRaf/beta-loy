package com.beta.loyalty.receipts.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReceiptClaimRequest(
        UUID venueId,
        String pib,
        String qrRaw,
        OffsetDateTime issuedAt,
        BigDecimal amount,
        String currency,
        String externalReceiptId
) {
}
