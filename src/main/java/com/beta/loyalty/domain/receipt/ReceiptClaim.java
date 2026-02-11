package com.beta.loyalty.domain.receipt;

import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.ReceiptClaimStatus;
import com.beta.loyalty.domain.tenant.TenantOwnedEntity;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "receipt_claims",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_claim_receipt",
                        columnNames = {"receipt_id"}
                )
        })
public class ReceiptClaim extends TenantOwnedEntity {
    //neko claim-uje da poeni pripadaju njemu
    //racunu se dodeljuje musterija
    //sadrzi skenirani racun i lokal

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_id", nullable = false)
    Receipt receipt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_customer_id", nullable = false)
    Customer initiatorCustomer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    ReceiptClaimStatus status = ReceiptClaimStatus.PENDING_SPLIT_ACCEPTANCE;

    @Column(nullable = false)
    long calculatedPointsTotal = 0;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime finalizedAt;

    @OneToMany(mappedBy = "receiptClaim", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    List<ReceiptClaimAllocation> allocations = new ArrayList<>();
}
