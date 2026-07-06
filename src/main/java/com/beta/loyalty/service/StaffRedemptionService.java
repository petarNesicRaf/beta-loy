package com.beta.loyalty.service;

import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.RedemptionRequest;
import com.beta.loyalty.dto.RedemptionRequestDto;
import com.beta.loyalty.repository.RedemptionRequestRepository;
import com.beta.loyalty.repository.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new SecurityException("Staff not assigned to venue");
        }

        return redemptionRequestRepository
                .findActivePendingByVenue(venueId, RedemptionStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
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
