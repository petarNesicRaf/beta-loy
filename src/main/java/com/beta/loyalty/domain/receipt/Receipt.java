package com.beta.loyalty.domain.receipt;

import com.beta.loyalty.domain.enums.ReceiptStatus;
import com.beta.loyalty.domain.tenant.TenantOwnedEntity;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
//moze da se skenira samo jedan racun, nema duplikata
@Table(name = "receipts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "ux_receipt_venue_hash",
                        columnNames = {"venue_id", "receipt_hash"}
                )
        })
public class Receipt extends TenantOwnedEntity {
    //fizicki fiskalni racun
    //ne pripada jos uvek musteriji
    //pripada samo lokalu
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @Column(length = 30)
    String pib;

    @Column(length = 120)
    String externalReceiptId;

    @Column(length = 128)
    String receiptHash;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime issuedAt;

    @Column(nullable = false, precision = 12, scale = 2)
    BigDecimal amount;

    @Column(nullable = false, length = 3)
    String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    ReceiptStatus status = ReceiptStatus.VALID;

    @OneToMany(mappedBy = "receipt", fetch = FetchType.LAZY)
    List<ReceiptClaim> claims = new ArrayList<>();
}
