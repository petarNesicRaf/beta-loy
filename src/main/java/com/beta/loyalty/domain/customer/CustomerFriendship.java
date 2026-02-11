package com.beta.loyalty.domain.customer;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
@Table(name = "customer_friendships",
        uniqueConstraints = @UniqueConstraint(name = "ux_friendship_pair", columnNames = {"customer_a_id", "customer_b_id"}))
public class CustomerFriendship extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_a_id", nullable = false)
    Customer customerA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_b_id", nullable = false)
    Customer customerB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    FriendshipStatus status = FriendshipStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_customer_id", nullable = false)
    Customer requestedBy;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime respondedAt;
}
