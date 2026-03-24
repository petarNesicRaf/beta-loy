package com.beta.loyalty.receipts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReceiptClaimRequest(
        @NotNull UUID venueId,
        @NotBlank String pib,
        String qrRaw,
        @NotNull OffsetDateTime issuedAt,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        String externalReceiptId
) {
}
