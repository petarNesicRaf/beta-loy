package com.beta.loyalty.domain.points;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "points_accounts",
        uniqueConstraints = @UniqueConstraint(name = "ux_points_account_customer_venue", columnNames = {"customer_id", "venue_id"}))
public class PointsAccount extends BaseEntity {
    //koliko poena imam u ovom lokalu?
    //customer, venue
    //UNIQUE (customer_id, venue_id)
    //svaki put kada se updateuje ledge i acc se updateuje da ne bi svaki put morao da se izracunava sum(point_delta)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @Column(nullable = false)
    long currentBalance = 0;

    @Version
    long version;

    @OneToMany(mappedBy = "pointsAccount", fetch = FetchType.LAZY)
    List<PointsLedgerEntry> ledgerEntries = new ArrayList<>();
}
