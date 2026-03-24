package com.beta.loyalty.redemptions.service;

import com.beta.loyalty.domain.enums.DecisionType;
import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.redemption.RedemptionDecision;
import com.beta.loyalty.domain.redemption.RedemptionRequest;
import com.beta.loyalty.domain.reward.Reward;
import com.beta.loyalty.domain.staff.StaffUser;
import com.beta.loyalty.points.service.PointsService;
import com.beta.loyalty.redemptions.dto.DecideRedemptionRequest;
import com.beta.loyalty.redemptions.dto.RedemptionRequestDto;
import com.beta.loyalty.redemptions.repository.RedemptionRequestRepository;
import com.beta.loyalty.rewards.rewards.RewardRepository;
import com.beta.loyalty.staff.repository.StaffUserRepository;
import com.beta.loyalty.venues.repository.VenueStaffAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedemptionDecisionService {
    private final RedemptionRequestRepository redemptionRequestRepository;
    private final VenueStaffAssignmentRepository staffAssignmentRepository;
    private final RewardRepository rewardRepository;
    private final StaffUserRepository staffUserRepository;
    private final PointsService pointsLedgerService;


    @Transactional
    public RedemptionRequestDto decide(UUID staffUserId, UUID redemptionRequestId, DecideRedemptionRequest req) {

        RedemptionRequest rr = redemptionRequestRepository.findByIdForUpdate(redemptionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("RedemptionRequest not found"));

        UUID venueId = rr.getVenue().getId();
        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new SecurityException("Staff not assigned to venue");
        }

        // idempotent: already decided
        if (rr.getDecision() != null) return toDto(rr);

        OffsetDateTime now = OffsetDateTime.now();

        // lazy expire
        if (rr.getStatus() == RedemptionStatus.PENDING && now.isAfter(rr.getExpiresAt())) {
            rr.setStatus(RedemptionStatus.EXPIRED);
            return toDto(rr);
        }

        if (rr.getStatus() != RedemptionStatus.PENDING) return toDto(rr);

        StaffUser staffRef = staffUserRepository.getReferenceById(staffUserId);

        RedemptionDecision decision = new RedemptionDecision();
        decision.setRedemptionRequest(rr);
        decision.setStaffUser(staffRef);
        decision.setDecision(req.decision());
        decision.setReason(req.reason());
        rr.setDecision(decision);

        if (req.decision() == DecisionType.REJECT) {
            rr.setStatus(RedemptionStatus.REJECTED);
            rr.setRejectedAt(now);
            return toDto(rr);
        }

        // APPROVE path
        Reward reward = rr.getReward(); // should be fine; else fetch by id

        // daily limit (hard check at approval time)
        if (reward.getPerCustomerLimitPerDay() != null) {
            OffsetDateTime from = now.toLocalDate().atStartOfDay().atOffset(now.getOffset());
            OffsetDateTime to = from.plusDays(1);

            long used = redemptionRequestRepository.countApprovedOrFulfilledInRange(
                    rr.getCustomer().getId(),
                    rr.getReward().getId(),
                    List.of(RedemptionStatus.APPROVED, RedemptionStatus.FULFILLED),
                    from,
                    to
            );

            if (used >= reward.getPerCustomerLimitPerDay()) {
                rr.setStatus(RedemptionStatus.REJECTED);
                rr.setRejectedAt(now);
                decision.setDecision(DecisionType.REJECT);
                decision.setReason("Daily limit reached");
                return toDto(rr);
            }
        }

        // stock check (atomic decrement if stock limited)
        if (reward.getStock() != null) {
            int updated = rewardRepository.decrementStockIfAvailable(reward.getId());
            if (updated != 1) {
                rr.setStatus(RedemptionStatus.REJECTED);
                rr.setRejectedAt(now);
                decision.setDecision(DecisionType.REJECT);
                decision.setReason("Out of stock");
                return toDto(rr);
            }
        }

        // debit points through ledger (idempotent by unique constraint + exists check)
        pointsLedgerService.debitForRedemption(
                rr.getTenantId(),
                rr.getCustomer().getId(),
                venueId,
                rr.getId(),
                rr.getPointsCostSnapshot()
        );

        rr.setStatus(RedemptionStatus.APPROVED);
        rr.setApprovedAt(now);

        return toDto(rr);
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
