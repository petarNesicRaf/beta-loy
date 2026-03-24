package com.beta.loyalty.redemptions.service;

import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.redemption.RedemptionRequest;
import com.beta.loyalty.redemptions.dto.RedemptionRequestDto;
import com.beta.loyalty.redemptions.repository.RedemptionRequestRepository;
import com.beta.loyalty.venues.repository.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedemptionFulfillmentService {
    private final RedemptionRequestRepository redemptionRequestRepository;
    private final VenueStaffAssignmentRepository staffAssignmentRepository;

    @Transactional
    public RedemptionRequestDto fulfill(UUID staffUserId, UUID redemptionRequestId) {

        RedemptionRequest rr = redemptionRequestRepository.findByIdForUpdate(redemptionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("RedemptionRequest not found"));

        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, rr.getVenue().getId())) {
            throw new SecurityException("Staff not assigned to venue");
        }

        if (rr.getStatus() != RedemptionStatus.APPROVED) {
            throw new IllegalStateException("Can only fulfill approved request");
        }

        rr.setStatus(RedemptionStatus.FULFILLED);
        rr.setFulfilledAt(OffsetDateTime.now());

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
