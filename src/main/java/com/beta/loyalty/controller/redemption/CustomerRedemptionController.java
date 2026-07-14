package com.beta.loyalty.controller.redemption;

import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.dto.redemption.CreateRedemptionRequestDto;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.service.redemption.RedemptionRequestService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer/redemptions")
@Tag(name = "Customer Redemption", description = "Request rewards and view redemption history")
public class CustomerRedemptionController {

    private final RedemptionRequestService redemptionRequestService;

    @PostMapping("/venues/{venueId}/create")
    public RedemptionRequestDto create(
            @PathVariable UUID venueId,
            @Valid @RequestBody CreateRedemptionRequestDto req
    ) {
        UUID currUuid = CurrentUser.requirePrincipal().userId();
        return redemptionRequestService.create(currUuid, venueId, req);
    }

    @GetMapping
    public Page<RedemptionRequestDto> myHistory(Pageable pageable) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        return redemptionRequestService.getMyHistory(customerId, pageable);
    }

    @GetMapping("/{id}")
    public RedemptionRequestDto myOne(@PathVariable UUID id) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        return redemptionRequestService.getMyOne(customerId, id);
    }
}
