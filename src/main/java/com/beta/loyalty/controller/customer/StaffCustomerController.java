package com.beta.loyalty.controller.customer;

import com.beta.loyalty.dto.customer.CustomerBalanceResponse;
import com.beta.loyalty.dto.customer.StaffCustomerResponse;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.security.AuthPrincipal;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.customer.StaffCustomerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff/customers")
@Tag(name = "Staff - Customer Lookup", description = "Search customers and inspect their balance and redemption history")
public class StaffCustomerController {

    private final StaffCustomerService staffCustomerService;

    @GetMapping("/search")
    public Page<StaffCustomerResponse> search(
            @RequestParam String q,
            Pageable pageable) {
        return staffCustomerService.search(q, pageable);
    }

    @GetMapping("/{customerId}/balance")
    public CustomerBalanceResponse getBalance(
            @PathVariable UUID customerId,
            @RequestParam UUID venueId) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffCustomerService.getBalance(p.userId(), customerId, venueId);
    }

    @GetMapping("/{customerId}/redemptions")
    public Page<RedemptionRequestDto> getRedemptions(
            @PathVariable UUID customerId,
            @RequestParam UUID venueId,
            Pageable pageable) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffCustomerService.getCustomerRedemptions(p.userId(), customerId, venueId, pageable);
    }
}
