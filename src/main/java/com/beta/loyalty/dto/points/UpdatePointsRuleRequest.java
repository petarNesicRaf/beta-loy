package com.beta.loyalty.dto.points;

import com.beta.loyalty.domain.enums.RoundingMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record UpdatePointsRuleRequest(
        @DecimalMin("0.0001")
        @Digits(integer = 8, fraction = 4)
        BigDecimal baseFactor,

        RoundingMode roundingMode,

        @Min(1) Integer claimWindowMinutes,

        @Min(1) Integer maxRecipientsPerReceipt,

        OffsetDateTime activeFrom,
        OffsetDateTime activeTo
) {
}
