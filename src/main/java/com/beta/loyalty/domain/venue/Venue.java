package com.beta.loyalty.domain.venue;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.VenueStatus;
import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.domain.points.PointsRule;
import com.beta.loyalty.domain.receipt.Receipt;
import com.beta.loyalty.domain.reward.Reward;
import com.beta.loyalty.domain.staff.VenueStaffAssignment;
import com.beta.loyalty.domain.tenant.Tenant;
import com.beta.loyalty.domain.tenant.TenantOwnedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "venues")
public class Venue extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    Tenant tenant;

    @Column(nullable = false, length = 200)
    String name;

    @Column(length = 30)
    String pib; // optional: venue-level PIB

    @Column(length = 400)
    String address;

    @Column(length = 120)
    String city;

    @Column(length = 80)
    String country;

    @Column(length = 80)
    String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    VenueStatus status = VenueStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    VenueType type = VenueType.DEFAULT;


    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    List<VenueStaffAssignment> staffAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    List<Reward> rewards = new ArrayList<>();

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    List<Receipt> receipts = new ArrayList<>();

    @OneToMany(mappedBy = "venue", fetch = FetchType.LAZY)
    List<PointsRule> pointsRules = new ArrayList<>();
}
