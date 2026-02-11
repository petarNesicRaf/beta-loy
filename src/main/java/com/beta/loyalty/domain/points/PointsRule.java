package com.beta.loyalty.domain.points;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.RoundingMode;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
@Table(name = "points_rule")
public class PointsRule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @Column(nullable = false, precision = 12, scale = 4)
    BigDecimal baseFactor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RoundingMode roundingMode = RoundingMode.FLOOR;

    @Column(nullable = false)
    int claimWindowMinutes = 1440;

    @Column(nullable = false)
    int maxRecipientsPerReceipt = 6;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime activeFrom;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime activeTo; // null = current
}
