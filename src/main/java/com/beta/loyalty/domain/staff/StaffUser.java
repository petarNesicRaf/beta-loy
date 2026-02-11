package com.beta.loyalty.domain.staff;

import com.beta.loyalty.domain.base.BaseEntity;
import com.beta.loyalty.domain.enums.StaffStatus;
import com.beta.loyalty.domain.tenant.Tenant;
import com.beta.loyalty.domain.tenant.TenantOwnedEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Getter
@Setter
@Entity
@Table(name = "staff_users")
public class StaffUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    Tenant tenant;

    @Column(length = 320)
    String email;

    @Column(length = 40)
    String phone;

    @Column(length = 255)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    StaffStatus status = StaffStatus.ACTIVE;

    @Column(columnDefinition = "timestamptz")
    OffsetDateTime lastLoginAt;



    @OneToMany(mappedBy = "staffUser", fetch = FetchType.LAZY)
    List<VenueStaffAssignment> venueAssignments = new ArrayList<>();



    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "staff_user_roles",
            joinColumns = @JoinColumn(name = "staff_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles = new HashSet<>();
}
