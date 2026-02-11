package com.beta.loyalty.domain.customer;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.CustomerStatus;
import com.beta.loyalty.domain.points.PointsAccount;
import com.beta.loyalty.domain.receipt.ReceiptClaim;
import com.beta.loyalty.domain.redemption.RedemptionRequest;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "customers")
public class Customer extends BaseEntity {
    @Column(length = 320)
    String email;

    @Column(length = 40)
    String phone;

    @Column(length = 120)
    String displayName;

    @Column(length = 255)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    CustomerStatus status = CustomerStatus.ACTIVE;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    List<PointsAccount> pointsAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "initiatorCustomer", fetch = FetchType.LAZY)
    List<ReceiptClaim> initiatedClaims = new ArrayList<>();

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    List<RedemptionRequest> redemptionRequests = new ArrayList<>();
}
