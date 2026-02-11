package com.beta.loyalty.domain.points;

import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.LedgerSourceType;
import com.beta.loyalty.domain.enums.LedgerType;
import com.beta.loyalty.domain.tenant.TenantOwnedEntity;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
@Getter
@Setter
@Entity
@Table(name = "points_ledger_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "ux_ledger_source_customer",
                columnNames = {"source_type", "source_id", "customer_id"}
        ))
public class PointsLedgerEntry extends TenantOwnedEntity {
        //istorija svih transakcija
        //po venue i customeru
        //npr: docker, Pera, +300, RECEIPT_CLAIM -> je jedna transakcija
        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "points_account_id", nullable = false)
        PointsAccount pointsAccount;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "venue_id", nullable = false)
        Venue venue;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "customer_id", nullable = false)
        Customer customer;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 20)
        LedgerType type;

        @Column(nullable = false)
        long pointsDelta;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 40)
        LedgerSourceType sourceType;

        @Column(nullable = false, columnDefinition = "uuid")
        UUID sourceId;
}
