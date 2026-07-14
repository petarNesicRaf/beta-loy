package com.beta.loyalty.dto.receipt;

import com.beta.loyalty.domain.Receipt;
import com.beta.loyalty.domain.enums.ReceiptStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReceiptResponse(
        UUID id,
        UUID venueId,
        String externalReceiptId,
        BigDecimal amount,
        String currency,
        OffsetDateTime issuedAt,
        ReceiptStatus status
) {
    public static ReceiptResponse from(Receipt r) {
        return new ReceiptResponse(
                r.getId(),
                r.getVenue().getId(),
                r.getExternalReceiptId(),
                r.getAmount(),
                r.getCurrency(),
                r.getIssuedAt(),
                r.getStatus()
        );
    }
}
