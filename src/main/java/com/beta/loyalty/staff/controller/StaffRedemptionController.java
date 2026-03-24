package com.beta.loyalty.staff.controller;

import com.beta.loyalty.auth.CurrentUser;
import com.beta.loyalty.redemptions.dto.DecideRedemptionRequest;
import com.beta.loyalty.redemptions.dto.RedemptionRequestDto;
import com.beta.loyalty.redemptions.service.RedemptionDecisionService;
import com.beta.loyalty.redemptions.service.RedemptionFulfillmentService;
import com.beta.loyalty.staff.service.StaffRedemptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff/redemptions")
@Tag(
        name = "Staff Redemption ",
        description = "Ovo je staff strana aplikacije, kada pristigne zahtev za dobijanje nagrade. Ovde staff ima metode" +
                " da prelista pending zahteve za nagrade(poll), da odluci sta ce da radi sa tom nagradom i da donese odluku."
)
public class StaffRedemptionController {
    private final RedemptionDecisionService decisionService;
    private final RedemptionFulfillmentService fulfillmentService;
    private final StaffRedemptionService staffRedemptionService;


    @PostMapping("/{id}/decide")
    public RedemptionRequestDto decide(
            @PathVariable UUID id,
            @Valid @RequestBody DecideRedemptionRequest req
    ) {
        UUID curr = CurrentUser.principal().get().userId();

        return decisionService.decide(curr, id, req);
    }

    @PostMapping("/{id}/fulfill")
    public RedemptionRequestDto fulfill(
            @PathVariable UUID id
    ) {
        UUID curr = CurrentUser.principal().get().userId();
        return fulfillmentService.fulfill(curr, id);
    }

    @GetMapping("/{venueId}/redemptions/pending")
    public List<RedemptionRequestDto> pending(
            @PathVariable UUID venueId
    ) {
        UUID curr = CurrentUser.principal().get().userId();

        return staffRedemptionService
                .getPending(curr, venueId);
    }
}
