package com.beta.loyalty.service.onboarding;

import com.beta.loyalty.domain.*;
import com.beta.loyalty.domain.enums.*;
import com.beta.loyalty.dto.onboarding.OnboardingRequest;
import com.beta.loyalty.dto.onboarding.OnboardingResponse;
import com.beta.loyalty.exception.ConflictException;
import com.beta.loyalty.repository.staff.RoleRepository;
import com.beta.loyalty.repository.staff.StaffUserRepository;
import com.beta.loyalty.repository.tenant.TenantRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final TenantRepository tenantRepository;
    private final StaffUserRepository staffUserRepository;
    private final RoleRepository roleRepository;
    private final VenueRepository venueRepository;
    private final VenueStaffAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public OnboardingResponse register(OnboardingRequest req) {
        if (staffUserRepository.existsByEmail(req.business().email())) {
            throw new ConflictException("Email already in use");
        }

        // 1. Tenant
        Tenant tenant = new Tenant();
        tenant.setName(req.business().name());
        tenant.setPib(req.business().pib());
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant = tenantRepository.save(tenant);

        // 2. OWNER role for this tenant
        Role ownerRole = new Role();
        ownerRole.setTenant(tenant);
        ownerRole.setName("OWNER");
        ownerRole = roleRepository.save(ownerRole);

        // 3. StaffUser (owner)
        StaffUser owner = new StaffUser();
        owner.setTenant(tenant);
        owner.setEmail(req.business().email());
        owner.setPasswordHash(passwordEncoder.encode(req.business().password()));
        owner.setStatus(StaffStatus.ACTIVE);
        owner.setRoles(Set.of(ownerRole));
        owner = staffUserRepository.save(owner);

        // 4. Venue
        OnboardingRequest.VenueInfo vi = req.venue();
        Venue venue = new Venue();
        venue.setTenant(tenant);
        venue.setName(vi.name());
        venue.setPib(req.business().pib());
        venue.setAddress(vi.address());
        venue.setCity(vi.city());
        venue.setCountry(vi.country() != null ? vi.country() : "RS");
        venue.setTimezone(vi.timezone());
        venue.setStatus(VenueStatus.ACTIVE);
        venue.setType(vi.type() != null ? vi.type() : VenueType.DEFAULT);
        venue = venueRepository.save(venue);

        // 5. Assign owner to venue
        VenueStaffAssignment assignment = new VenueStaffAssignment();
        assignment.setVenue(venue);
        assignment.setStaffUser(owner);
        assignment.setActive(true);
        assignmentRepository.save(assignment);

        return new OnboardingResponse(
                tenant.getId(),
                tenant.getName(),
                owner.getId(),
                owner.getEmail(),
                venue.getId(),
                venue.getName(),
                true
        );
    }
}
