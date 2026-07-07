package com.beta.loyalty.domain;

import com.beta.loyalty.security.UserType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    UUID token;

    @Column(nullable = false, columnDefinition = "uuid")
    UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    UserType userType;

    @Column(columnDefinition = "uuid")
    UUID tenantId;

    // comma-separated roles, e.g. "ROLE_CUSTOMER" or "ROLE_STAFF,ROLE_OWNER"
    @Column(nullable = false, length = 500)
    String roles;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime expiresAt;

    @Column(nullable = false)
    boolean revoked = false;
}
