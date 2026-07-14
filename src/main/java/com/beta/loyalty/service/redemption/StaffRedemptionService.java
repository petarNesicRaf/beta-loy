package com.beta.loyalty.service.redemption;

import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.RedemptionRequest;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.exception.ForbiddenException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.redemption.RedemptionRequestRepository;
import com.beta.loyalty.repository.venue.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffRedemptionService {
    private final RedemptionRequestRepository redemptionRequestRepository;
    private final VenueStaffAssignmentRepository staffAssignmentRepository;

    public List<RedemptionRequestDto> getPending(UUID staffUserId, UUID venueId) {

        if (!staffAssignmentRepository
                .existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new com.beta.loyalty.exception.ForbiddenException("Staff not assigned to venue");
        }

        return redemptionRequestRepository
                .findActivePendingByVenue(venueId, RedemptionStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public Page<RedemptionRequestDto> getHistory(
            UUID staffUserId,
            UUID tenantId,
            UUID venueId,
            RedemptionStatus status,
            OffsetDateTime from,
            OffsetDateTime to,
            Pageable pageable) {

        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to venue");
        }

        return redemptionRequestRepository
                .findHistory(venueId, tenantId, status, from, to, pageable)
                .map(this::toDto);
    }

    public RedemptionRequestDto getOne(UUID staffUserId, UUID tenantId, UUID venueId, UUID redemptionId) {

        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to venue");
        }

        return redemptionRequestRepository
                .findByIdAndVenueIdAndTenantId(redemptionId, venueId, tenantId)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("Redemption not found"));
    }

    private RedemptionRequestDto toDto(RedemptionRequest rr) {
        return new RedemptionRequestDto(
                rr.getId(),
                rr.getVenue().getId(),
                rr.getReward().getId(),
                rr.getCustomer().getId(),
                rr.getStatus(),
                rr.getPointsCostSnapshot(),
                rr.getRequestedAt(),
                rr.getExpiresAt(),
                rr.getApprovedAt(),
                rr.getRejectedAt(),
                rr.getFulfilledAt()
        );
    }
}
