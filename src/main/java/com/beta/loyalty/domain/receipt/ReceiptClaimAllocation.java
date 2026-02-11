package com.beta.loyalty.domain.receipt;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.AllocationStatus;
import com.beta.loyalty.domain.enums.ShareType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
@Table(name = "receipt_claim_allocations",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_allocation_claim_recipient",
                columnNames = {"receipt_claim_id", "recipient_customer_id"}
        ))
public class ReceiptClaimAllocation extends BaseEntity {
    //kako se dele poeni ovog racuna
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receipt_claim_id", nullable = false)
    ReceiptClaim receiptClaim;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_customer_id", nullable = false)
    Customer recipientCustomer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    ShareType shareType = ShareType.EQUAL;

    @Column(precision = 10, scale = 4)
    BigDecimal shareValue; // only for CUSTOM

    @Column(nullable = false)
    long allocatedPoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AllocationStatus status = AllocationStatus.PENDING;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime respondedAt;
}
