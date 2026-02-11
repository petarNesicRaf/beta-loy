package com.beta.loyalty.domain.tenant;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.TenantStatus;
import com.beta.loyalty.domain.staff.StaffUser;
import com.beta.loyalty.domain.venue.Venue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@Entity
@Table(name = "tenants")
public class Tenant extends BaseEntity {
    @Column(nullable = false, length = 200)
    String name;

    @Column(length = 300)
    String legalName;

    @Column(length = 30)
    String pib;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    TenantStatus status = TenantStatus.ACTIVE;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    List<Venue> venues = new ArrayList<>();

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY)
    List<StaffUser> staffUsers = new ArrayList<>();

}
