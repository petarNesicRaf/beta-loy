package com.beta.loyalty.domain.redemption;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.DecisionType;
import com.beta.loyalty.domain.staff.StaffUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
@Getter
@Setter
@Entity
@Table(name = "redemption_decisions",
        uniqueConstraints = @UniqueConstraint(name = "ux_decision_redemption", columnNames = {"redemption_request_id"}))
public class RedemptionDecision extends BaseEntity {
    //staff odlucuje da li dobija nagradu ili ne
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "redemption_request_id", nullable = false)
    RedemptionRequest redemptionRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "staff_user_id", nullable = false)
    StaffUser staffUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    DecisionType decision;

    @Column(length = 300)
    String reason;

    @Column(nullable = false, columnDefinition = "timestamptz")
    OffsetDateTime decidedAt = OffsetDateTime.now();
}
