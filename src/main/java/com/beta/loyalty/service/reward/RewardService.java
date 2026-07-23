package com.beta.loyalty.service.reward;

import com.beta.loyalty.domain.Reward;
import com.beta.loyalty.domain.Venue;
import com.beta.loyalty.domain.enums.RewardStatus;
import com.beta.loyalty.dto.reward.RewardCreateRequest;
import com.beta.loyalty.dto.reward.RewardPublicResponse;
import com.beta.loyalty.dto.reward.UpdateRewardRequest;
import com.beta.loyalty.dto.reward.UpdateRewardStatusRequest;
import com.beta.loyalty.exception.ForbiddenException;
import com.beta.loyalty.exception.NotFoundException;
import com.beta.loyalty.repository.reward.RewardRepository;
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
public class RewardService {
    private final RewardRepository rewardRepository;
    private final VenueRepository venueRepository;
    private final VenueStaffAssignmentRepository staffAssignmentRepository;

    @CacheEvict(value = "venue-rewards", allEntries = true)
    @Transactional
    public RewardPublicResponse createReward(UUID staffUserId, UUID tenantId, UUID venueId, RewardCreateRequest req) {
        Venue venue = venueRepository.findByIdAndTenantId(venueId, tenantId)
                .orElseThrow(() -> new NotFoundException("Venue not found"));

        requireVenueAssignment(staffUserId, venueId);

        Reward reward = new Reward();
        reward.setVenue(venue);
        reward.setName(req.name());
        reward.setDescription(req.description());
        reward.setPointsCost(req.pointsCost());
        reward.setValidFrom(req.validFrom());
        reward.setValidTo(req.validTo());
        reward.setStock(req.stock());
        reward.setPerCustomerLimitPerDay(req.perCustomerLimitPerDay());
        reward.setTier(req.tier());
        reward.setStatus(RewardStatus.ACTIVE);

        return RewardPublicResponse.from(rewardRepository.save(reward));
    }

    @Transactional(readOnly = true)
    public Page<RewardPublicResponse> listRewards(UUID tenantId, UUID venueId, Pageable pageable) {
        return rewardRepository.findAllByVenueIdAndTenantId(venueId, tenantId, pageable)
                .map(RewardPublicResponse::from);
    }

    @Transactional(readOnly = true)
    public RewardPublicResponse getReward(UUID tenantId, UUID id) {
        Reward reward = rewardRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Reward not found"));
        return RewardPublicResponse.from(reward);
    }

    @CacheEvict(value = "venue-rewards", allEntries = true)
    @Transactional
    public RewardPublicResponse updateReward(UUID staffUserId, UUID tenantId, UUID id, UpdateRewardRequest req) {
        Reward reward = rewardRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Reward not found"));

        requireVenueAssignment(staffUserId, reward.getVenue().getId());

        if (req.name() != null) reward.setName(req.name());
        if (req.description() != null) reward.setDescription(req.description());
        if (req.pointsCost() != null) reward.setPointsCost(req.pointsCost());
        if (req.validFrom() != null) reward.setValidFrom(req.validFrom());
        if (req.validTo() != null) reward.setValidTo(req.validTo());
        if (req.stock() != null) reward.setStock(req.stock());
        if (req.perCustomerLimitPerDay() != null) reward.setPerCustomerLimitPerDay(req.perCustomerLimitPerDay());
        if (req.tier() != null) reward.setTier(req.tier());

        return RewardPublicResponse.from(reward);
    }

    @CacheEvict(value = "venue-rewards", allEntries = true)
    @Transactional
    public RewardPublicResponse updateRewardStatus(UUID staffUserId, UUID tenantId, UUID id, UpdateRewardStatusRequest req) {
        Reward reward = rewardRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Reward not found"));

        requireVenueAssignment(staffUserId, reward.getVenue().getId());

        reward.setStatus(req.status());
        return RewardPublicResponse.from(reward);
    }

    @CacheEvict(value = "venue-rewards", allEntries = true)
    @Transactional
    public void deleteReward(UUID staffUserId, UUID tenantId, UUID id) {
        Reward reward = rewardRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Reward not found"));

        requireVenueAssignment(staffUserId, reward.getVenue().getId());

        reward.setStatus(RewardStatus.INACTIVE);
    }

    private void requireVenueAssignment(UUID staffUserId, UUID venueId) {
        if (!staffAssignmentRepository.existsByStaffUserIdAndVenueIdAndActiveTrue(staffUserId, venueId)) {
            throw new ForbiddenException("Staff not assigned to this venue");
        }
    }
}
