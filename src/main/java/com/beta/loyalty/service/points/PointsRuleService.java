package com.beta.loyalty.service.points;

import com.beta.loyalty.domain.PointsRule;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.RoundingMode;
import com.beta.loyalty.dto.points.CreatePointsRuleRequest;
import com.beta.loyalty.dto.points.PointsRuleResponse;
import com.beta.loyalty.dto.points.UpdatePointsRuleRequest;
import com.beta.loyalty.exception.ForbiddenException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.points.PointsRuleRepository;
import com.beta.loyalty.repository.venue.VenueRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PointsRuleService {
    private final PointsRuleRepository pointsRuleRepository;
    private final VenueRepository venueRepository;
    private final VenueStaffAssignmentRepository staffAssignmentRepository;

    @Transactional
    public PointsRuleResponse createRule(UUID staffUserId, UUID tenantId, UUID venueId, CreatePointsRuleRequest req) {
        Venue venue = findVenueForTenant(tenantId, venueId);
        requireVenueAssignment(staffUserId, venueId);

        PointsRule rule = new PointsRule();
        rule.setVenue(venue);
        rule.setBaseFactor(req.baseFactor());
        rule.setRoundingMode(req.roundingMode() != null ? req.roundingMode() : RoundingMode.FLOOR);
        rule.setClaimWindowMinutes(req.claimWindowMinutes() != null ? req.claimWindowMinutes() : 1440);
        rule.setMaxRecipientsPerReceipt(req.maxRecipientsPerReceipt() != null ? req.maxRecipientsPerReceipt() : 6);
        rule.setActiveFrom(req.activeFrom());
        rule.setActiveTo(req.activeTo());

        return PointsRuleResponse.from(pointsRuleRepository.save(rule));
    }

    @Transactional(readOnly = true)
    public Page<PointsRuleResponse> listRules(UUID tenantId, UUID venueId, Pageable pageable) {
        findVenueForTenant(tenantId, venueId);
        return pointsRuleRepository.findAllByVenueId(venueId, pageable).map(PointsRuleResponse::from);
    }

    @Transactional
    public PointsRuleResponse updateRule(UUID staffUserId, UUID tenantId, UUID id, UpdatePointsRuleRequest req) {
        PointsRule rule = findRuleForTenant(tenantId, id);
        requireVenueAssignment(staffUserId, rule.getVenue().getId());

        if (req.baseFactor() != null) rule.setBaseFactor(req.baseFactor());
        if (req.roundingMode() != null) rule.setRoundingMode(req.roundingMode());
        if (req.claimWindowMinutes() != null) rule.setClaimWindowMinutes(req.claimWindowMinutes());
        if (req.maxRecipientsPerReceipt() != null) rule.setMaxRecipientsPerReceipt(req.maxRecipientsPerReceipt());
        if (req.activeFrom() != null) rule.setActiveFrom(req.activeFrom());
        if (req.activeTo() != null) rule.setActiveTo(req.activeTo());

        return PointsRuleResponse.from(rule);
    }

    @Transactional
    public void deleteRule(UUID staffUserId, UUID tenantId, UUID id) {
        PointsRule rule = findRuleForTenant(tenantId, id);
        requireVenueAssignment(staffUserId, rule.getVenue().getId());
        pointsRuleRepository.delete(rule);
    }

    private Venue findVenueForTenant(UUID tenantId, UUID venueId) {
        return venueRepository.findByIdAndTenantId(venueId, tenantId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));
    }

    private PointsRule findRuleForTenant(UUID tenantId, UUID id) {
        return pointsRuleRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Points rule not found"));
    }

    private void requireVenueAssignment(UUID staffUserId, UUID venueId) {
        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to this venue");
        }
    }
}
