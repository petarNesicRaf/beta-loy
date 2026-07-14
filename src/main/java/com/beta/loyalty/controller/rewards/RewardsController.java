package com.beta.loyalty.controller.rewards;

import com.beta.loyalty.dto.reward.RewardCreateRequest;
import com.beta.loyalty.dto.reward.RewardPublicResponse;
import com.beta.loyalty.dto.reward.UpdateRewardRequest;
import com.beta.loyalty.dto.reward.UpdateRewardStatusRequest;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.reward.RewardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff")
@Tag(name = "Staff Rewards", description = "Reward management for staff")
public class RewardsController {
    private final RewardService rewardService;

    @PostMapping("/venues/{venueId}/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    public RewardPublicResponse createReward(
            @PathVariable UUID venueId,
            @Valid @RequestBody RewardCreateRequest req) {
        var p = CurrentUser.requirePrincipal();
        return rewardService.createReward(p.userId(), p.tenantId(), venueId, req);
    }

    @GetMapping("/venues/{venueId}/rewards")
    public Page<RewardPublicResponse> listRewards(
            @PathVariable UUID venueId,
            Pageable pageable) {
        return rewardService.listRewards(CurrentUser.requirePrincipal().tenantId(), venueId, pageable);
    }

    @GetMapping("/rewards/{id}")
    public RewardPublicResponse getReward(@PathVariable UUID id) {
        return rewardService.getReward(CurrentUser.requirePrincipal().tenantId(), id);
    }

    @PatchMapping("/rewards/{id}")
    public RewardPublicResponse updateReward(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRewardRequest req) {
        var p = CurrentUser.requirePrincipal();
        return rewardService.updateReward(p.userId(), p.tenantId(), id, req);
    }

    @PatchMapping("/rewards/{id}/status")
    public RewardPublicResponse updateRewardStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRewardStatusRequest req) {
        var p = CurrentUser.requirePrincipal();
        return rewardService.updateRewardStatus(p.userId(), p.tenantId(), id, req);
    }

    @DeleteMapping("/rewards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReward(@PathVariable UUID id) {
        var p = CurrentUser.requirePrincipal();
        rewardService.deleteReward(p.userId(), p.tenantId(), id);
    }
}
