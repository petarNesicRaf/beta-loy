package com.beta.loyalty.domain.reward;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.domain.redemption.RedemptionRequest;
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
@Table(name = "rewards")
public class Reward extends BaseEntity {
    //promocije koje su vezani za lokal
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @Column(nullable = false, length = 160)
    String name;

    @Column(length = 1000)
    String description;

    @Column(nullable = false)
    long pointsCost;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    RewardStatus status = RewardStatus.ACTIVE;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime validFrom;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime validTo;

    Integer stock;
    Integer perCustomerLimitPerDay;

    @OneToMany(mappedBy = "reward", fetch = FetchType.LAZY)
    List<RedemptionRequest> redemptionRequests = new ArrayList<>();
}
