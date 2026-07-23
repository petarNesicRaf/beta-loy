package com.beta.loyalty.service.venue;

import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.VenueType;
import com.beta.loyalty.dto.venue.StaffVenueResponse;
import com.beta.loyalty.dto.venue.UpdateVenueRequest;
import com.beta.loyalty.dto.venue.UpdateVenueStatusRequest;
import com.beta.loyalty.dto.venue.VenueCreateRequest;
import com.beta.loyalty.exception.ForbiddenException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.tenant.TenantRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffVenueService {
    private final VenueRepository venueRepository;
    private final VenueStaffAssignmentRepository staffAssignmentRepository;
    private final TenantRepository tenantRepository;

    @CacheEvict(value = "venues", allEntries = true)
    @Transactional
    public StaffVenueResponse createVenue(UUID tenantId, VenueCreateRequest req) {
        Venue venue = new Venue();
        venue.setTenant(tenantRepository.getReferenceById(tenantId));
        venue.setName(req.name());
        venue.setPib(req.pib());
        venue.setAddress(req.address());
        venue.setCity(req.city());
        venue.setCountry(req.country());
        venue.setTimezone(req.timezone());
        venue.setType(req.type() != null ? req.type() : VenueType.DEFAULT);

        return StaffVenueResponse.from(venueRepository.save(venue));
    }

    @Transactional(readOnly = true)
    public Page<StaffVenueResponse> listVenues(UUID tenantId, Pageable pageable) {
        return venueRepository.findAllByTenantId(tenantId, pageable)
                .map(StaffVenueResponse::from);
    }

    @Transactional(readOnly = true)
    public StaffVenueResponse getVenue(UUID tenantId, UUID id) {
        return StaffVenueResponse.from(findForTenant(tenantId, id));
    }

    @CacheEvict(value = "venues", allEntries = true)
    @Transactional
    public StaffVenueResponse updateVenue(UUID staffUserId, UUID tenantId, UUID id, UpdateVenueRequest req) {
        Venue venue = findForTenant(tenantId, id);
        requireVenueAssignment(staffUserId, id);

        if (req.name() != null) venue.setName(req.name());
        if (req.pib() != null) venue.setPib(req.pib());
        if (req.address() != null) venue.setAddress(req.address());
        if (req.city() != null) venue.setCity(req.city());
        if (req.country() != null) venue.setCountry(req.country());
        if (req.timezone() != null) venue.setTimezone(req.timezone());
        if (req.type() != null) venue.setType(req.type());

        return StaffVenueResponse.from(venue);
    }

    @CacheEvict(value = "venues", allEntries = true)
    @Transactional
    public StaffVenueResponse updateVenueStatus(UUID staffUserId, UUID tenantId, UUID id, UpdateVenueStatusRequest req) {
        Venue venue = findForTenant(tenantId, id);
        requireVenueAssignment(staffUserId, id);

        venue.setStatus(req.status());
        return StaffVenueResponse.from(venue);
    }

    private Venue findForTenant(UUID tenantId, UUID id) {
        return venueRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
    }

    private void requireVenueAssignment(UUID staffUserId, UUID venueId) {
        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to this venue");
        }
    }

}
