package com.beta.loyalty.domain.redemption;

import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.reward.Reward;
import com.beta.loyalty.domain.tenant.TenantOwnedEntity;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
@Table(name = "redemption_requests")
public class RedemptionRequest extends TenantOwnedEntity {
    //ko trazi nagradu
    //koji lokal
    //koja nagrada
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reward_id", nullable = false)
    Reward reward;

    @Column(nullable = false)
    long pointsCostSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RedemptionStatus status = RedemptionStatus.PENDING;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime expiresAt;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime approvedAt;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime rejectedAt;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime fulfilledAt;

    @Column(length = 500)
    String customerNote;

    @OneToOne(mappedBy = "redemptionRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    RedemptionDecision decision;
}
