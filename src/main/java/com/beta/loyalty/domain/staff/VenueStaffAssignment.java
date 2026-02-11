package com.beta.loyalty.domain.staff;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
@Table(name = "venue_staff_assignments",
        uniqueConstraints = @UniqueConstraint(name = "ux_vsa_venue_staff", columnNames = {"venue_id", "staff_user_id"}))
public class VenueStaffAssignment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    Venue venue;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false)
    StaffUser staffUser;

    @Column(nullable = false)
    boolean isActive = true;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime assignedAt = OffsetDateTime.now();
}
