package com.beta.loyalty.controller.customer;

import com.beta.loyalty.dto.customer.ChangeCustomerPasswordRequest;
import com.beta.loyalty.dto.customer.CustomerMeResponse;
import com.beta.loyalty.dto.customer.CustomerVenuePointsAccount;
import com.beta.loyalty.dto.customer.UpdateCustomerProfileRequest;
import com.beta.loyalty.dto.points.LedgerEntryResponse;
import com.beta.loyalty.dto.receipt.CustomerReceiptClaimResponse;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.customer.CustomerService;
import com.beta.loyalty.service.points.CustomerLedgerService;
import com.beta.loyalty.service.receipt.CustomerReceiptHistoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customer")
@Tag(name = "Customer", description = "Profile, points balance and ledger history")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    private final CustomerLedgerService customerLedgerService;
    private final CustomerReceiptHistoryService receiptHistoryService;

    @GetMapping("/me")
    public CustomerMeResponse me() {
        return customerService.me();
    }

    @PatchMapping("/me")
    public CustomerMeResponse updateProfile(@Valid @RequestBody UpdateCustomerProfileRequest req) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        return customerService.updateProfile(customerId, req);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangeCustomerPasswordRequest req) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        customerService.changePassword(customerId, req);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount() {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        customerService.deleteAccount(customerId);
    }

    @GetMapping("/points-per-venue")
    public List<CustomerVenuePointsAccount> pointsPerVenue() {
        return customerService.pointsAccPerVenue();
    }

    @GetMapping("/points/history")
    public Page<LedgerEntryResponse> pointsHistory(
            @RequestParam(required = false) UUID venueId,
            Pageable pageable) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        return customerLedgerService.getHistory(customerId, venueId, pageable);
    }

    @GetMapping("/receipts")
    public Page<CustomerReceiptClaimResponse> receiptHistory(Pageable pageable) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        return receiptHistoryService.getHistory(customerId, pageable);
    }

    @GetMapping("/receipts/{claimId}")
    public CustomerReceiptClaimResponse receiptOne(@PathVariable UUID claimId) {
        UUID customerId = CurrentUser.requirePrincipal().userId();
        return receiptHistoryService.getOne(customerId, claimId);
    }
}
