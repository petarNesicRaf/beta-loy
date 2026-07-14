package com.beta.loyalty.dto.staff;

import com.beta.loyalty.domain.enums.StaffStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStaffStatusRequest(
        @NotNull StaffStatus status
) {
}
