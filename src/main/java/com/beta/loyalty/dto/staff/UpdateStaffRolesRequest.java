package com.beta.loyalty.dto.staff;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateStaffRolesRequest(
        @NotNull @NotEmpty Set<String> roles
) {
}
