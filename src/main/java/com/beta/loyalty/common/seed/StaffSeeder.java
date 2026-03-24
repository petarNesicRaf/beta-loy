package com.beta.loyalty.common.seed;

import com.beta.loyalty.domain.enums.StaffStatus;
import com.beta.loyalty.domain.staff.Role;
import com.beta.loyalty.domain.staff.StaffUser;
import com.beta.loyalty.domain.staff.VenueStaffAssignment;
import com.beta.loyalty.domain.tenant.Tenant;
import com.beta.loyalty.domain.venue.Venue;
import com.beta.loyalty.staff.repository.RoleRepository;
import com.beta.loyalty.staff.repository.StaffUserRepository;
import com.beta.loyalty.tenants.repository.TenantRepository;
import com.beta.loyalty.venues.repository.VenueRepository;
import com.beta.loyalty.venues.repository.VenueStaffAssignmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seeds staff users, roles, and venue assignments.
 * Runs after TenantVenueSeeder (Order 1).
 *
 * Staff credentials (all share the same password for dev convenience):
 *   docker.owner@example.com   / Staff123!  — OWNER,   Docker Dorcol + Docker NBG
 *   docker.manager@example.com / Staff123!  — MANAGER, Docker Dorcol
 *   docker.cashier@example.com / Staff123!  — CASHIER, Docker Dorcol + Docker NBG
 *   saint.owner@example.com    / Staff123!  — OWNER,   Saint Stadion + Saint Bulevar
 *   saint.manager@example.com  / Staff123!  — MANAGER, Saint Stadion
 *   saint.cashier@example.com  / Staff123!  — CASHIER, Saint Bulevar
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class StaffSeeder implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final VenueRepository venueRepository;
    private final StaffUserRepository staffUserRepository;
    private final RoleRepository roleRepository;
    private final VenueStaffAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.enabled:false}")
    private boolean enabled;

    private static final String STAFF_PASSWORD = "Staff123!";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        Tenant docker = tenantRepository.findByNameIgnoreCase("Docker Corp").orElse(null);
        Tenant saint  = tenantRepository.findByNameIgnoreCase("Saint Tenant").orElse(null);
        if (docker == null || saint == null) return;

        seedDockerStaff(docker);
        seedSaintStaff(saint);
    }

    private void seedDockerStaff(Tenant docker) {
        Role ownerRole   = seedRole(docker, "OWNER");
        Role managerRole = seedRole(docker, "MANAGER");
        Role cashierRole = seedRole(docker, "CASHIER");

        StaffUser owner   = seedStaff(docker, "docker.owner@example.com",   "Docker Owner",   Set.of(ownerRole));
        StaffUser manager = seedStaff(docker, "docker.manager@example.com", "Docker Manager", Set.of(managerRole));
        StaffUser cashier = seedStaff(docker, "docker.cashier@example.com", "Docker Cashier", Set.of(cashierRole));

        venueRepository.findByTenantAndNameIgnoreCase(docker, "Docker Dorcol").ifPresent(v -> {
            assign(owner,   v);
            assign(manager, v);
            assign(cashier, v);
        });
        venueRepository.findByTenantAndNameIgnoreCase(docker, "Docker NBG").ifPresent(v -> {
            assign(owner,   v);
            assign(cashier, v);
        });
    }

    private void seedSaintStaff(Tenant saint) {
        Role ownerRole   = seedRole(saint, "OWNER");
        Role managerRole = seedRole(saint, "MANAGER");
        Role cashierRole = seedRole(saint, "CASHIER");

        StaffUser owner   = seedStaff(saint, "saint.owner@example.com",   "Saint Owner",   Set.of(ownerRole));
        StaffUser manager = seedStaff(saint, "saint.manager@example.com", "Saint Manager", Set.of(managerRole));
        StaffUser cashier = seedStaff(saint, "saint.cashier@example.com", "Saint Cashier", Set.of(cashierRole));

        venueRepository.findByTenantAndNameIgnoreCase(saint, "Saint Stadion").ifPresent(v -> {
            assign(owner,   v);
            assign(manager, v);
        });
        venueRepository.findByTenantAndNameIgnoreCase(saint, "Saint Bulevar").ifPresent(v -> {
            assign(owner,   v);
            assign(cashier, v);
        });
    }

    private Role seedRole(Tenant tenant, String name) {
        return roleRepository.findByTenantAndName(tenant, name).orElseGet(() -> {
            Role r = new Role();
            r.setTenant(tenant);
            r.setName(name);
            return roleRepository.save(r);
        });
    }

    private StaffUser seedStaff(Tenant tenant, String email, String displayName, Set<Role> roles) {
        return staffUserRepository.findByEmail(email).orElseGet(() -> {
            StaffUser s = new StaffUser();
            s.setTenant(tenant);
            s.setEmail(email);
            s.setPasswordHash(passwordEncoder.encode(STAFF_PASSWORD));
            s.setStatus(StaffStatus.ACTIVE);
            s.getRoles().addAll(roles);
            StaffUser saved = staffUserRepository.save(s);
            System.out.println("[SEED] Staff created: " + email + " (" + tenant.getName() + ")");
            return saved;
        });
    }

    private void assign(StaffUser staff, Venue venue) {
        if (assignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staff.getId(), venue.getId())) return;
        VenueStaffAssignment a = new VenueStaffAssignment();
        a.setStaffUser(staff);
        a.setVenue(venue);
        a.setActive(true);
        assignmentRepository.save(a);
        System.out.println("[SEED] Assigned " + staff.getEmail() + " → " + venue.getName());
    }
}