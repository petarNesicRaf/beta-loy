package com.beta.loyalty.dto.staff;

import com.beta.loyalty.domain.Role;
import com.beta.loyalty.domain.StaffUser;
import com.beta.loyalty.domain.enums.StaffStatus;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record StaffProfileResponse(
        UUID id,
        String email,
        StaffStatus status,
        Set<String> roles,
        OffsetDateTime lastLoginAt
) {
    public static StaffProfileResponse from(StaffUser u) {
        return new StaffProfileResponse(
                u.getId(),
                u.getEmail(),
                u.getStatus(),
                u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                u.getLastLoginAt()
        );
    }
}
