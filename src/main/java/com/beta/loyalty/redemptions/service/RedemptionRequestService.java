package com.beta.loyalty.redemptions.service;

import com.beta.loyalty.customer.repository.CustomerRepository;
import com.beta.loyalty.domain.customer.Customer;
import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.domain.redemption.RedemptionRequest;
import com.beta.loyalty.domain.reward.Reward;
import com.beta.loyalty.redemptions.dto.CreateRedemptionRequestDto;
import com.beta.loyalty.redemptions.dto.RedemptionRequestDto;
import com.beta.loyalty.redemptions.repository.RedemptionRequestRepository;
import com.beta.loyalty.rewards.rewards.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedemptionRequestService {
    private final RewardRepository rewardRepository;
    private final RedemptionRequestRepository redemptionRequestRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public RedemptionRequestDto create(UUID customerId, UUID venueId, CreateRedemptionRequestDto req) {

        if (req.idempotencyKey() != null && !req.idempotencyKey().isBlank()) {
            var existing = redemptionRequestRepository.findByCustomerIdAndIdempotencyKey(customerId, req.idempotencyKey());
            if (existing.isPresent()) return toDto(existing.get());
        }

        Reward reward = rewardRepository.findByIdAndVenueId(req.rewardId(), venueId)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found for venue"));

        if (reward.getStatus() != RewardStatus.ACTIVE) throw new IllegalStateException("Reward not active");

        OffsetDateTime now = OffsetDateTime.now();
        if (reward.getValidFrom() != null && now.isBefore(reward.getValidFrom())) throw new IllegalStateException("Reward not started");
        if (reward.getValidTo() != null && now.isAfter(reward.getValidTo())) throw new IllegalStateException("Reward expired");

        Customer customerRef = customerRepository.getReferenceById(customerId);

        RedemptionRequest rr = new RedemptionRequest();
        rr.setTenantId(reward.getVenue().getTenant().getId()); // ensure venue has tenant loaded or use tenantId field if you store it
        rr.setVenue(reward.getVenue());
        rr.setCustomer(customerRef);
        rr.setReward(reward);

        rr.setPointsCostSnapshot(reward.getPointsCost());
        rr.setStatus(RedemptionStatus.PENDING);
        rr.setRequestedAt(now);
        rr.setExpiresAt(now.plusMinutes(5));

        rr.setCustomerNote(req.customerNote());
        rr.setIdempotencyKey(req.idempotencyKey());

        redemptionRequestRepository.save(rr);
        return toDto(rr);
    }

    public RedemptionRequestDto toDto(RedemptionRequest rr) {
        return new RedemptionRequestDto(
                rr.getId(),
                rr.getVenue().getId(),
                rr.getReward().getId(),
                rr.getCustomer().getId(),
                rr.getStatus(),
                rr.getPointsCostSnapshot(),
//                rr.getRedemptionCode(),
                rr.getRequestedAt(),
                rr.getExpiresAt(),
                rr.getApprovedAt(),
                rr.getRejectedAt(),
                rr.getFulfilledAt()
        );
    }

}
