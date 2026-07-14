package com.beta.loyalty.controller.points;

import com.beta.loyalty.dto.points.CreatePointsRuleRequest;
import com.beta.loyalty.dto.points.PointsRuleResponse;
import com.beta.loyalty.dto.points.UpdatePointsRuleRequest;
import com.beta.loyalty.security.AuthPrincipal;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.points.PointsRuleService;
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
@Tag(name = "Points Rules", description = "Points earning rule management per venue")
public class PointsRuleController {
    private final PointsRuleService pointsRuleService;

    @PostMapping("/venues/{venueId}/points-rules")
    @ResponseStatus(HttpStatus.CREATED)
    public PointsRuleResponse createRule(
            @PathVariable UUID venueId,
            @Valid @RequestBody CreatePointsRuleRequest req) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return pointsRuleService.createRule(p.userId(), p.tenantId(), venueId, req);
    }

    @GetMapping("/venues/{venueId}/points-rules")
    public Page<PointsRuleResponse> listRules(
            @PathVariable UUID venueId,
            Pageable pageable) {
        return pointsRuleService.listRules(CurrentUser.requirePrincipal().tenantId(), venueId, pageable);
    }

    @PutMapping("/points-rules/{id}")
    public PointsRuleResponse updateRule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePointsRuleRequest req) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return pointsRuleService.updateRule(p.userId(), p.tenantId(), id, req);
    }

    @DeleteMapping("/points-rules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRule(@PathVariable UUID id) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        pointsRuleService.deleteRule(p.userId(), p.tenantId(), id);
    }
}
