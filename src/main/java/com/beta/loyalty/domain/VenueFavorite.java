package com.beta.loyalty.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "venue_favorites",
        uniqueConstraints = @UniqueConstraint(name = "ux_venue_favorite_customer_venue", columnNames = {"customer_id", "venue_id"}))
public class VenueFavorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;
}
