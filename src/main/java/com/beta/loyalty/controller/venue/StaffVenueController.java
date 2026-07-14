package com.beta.loyalty.controller.venue;

import com.beta.loyalty.domain.enums.RedemptionStatus;
import com.beta.loyalty.dto.receipt.ReceiptResponse;
import com.beta.loyalty.dto.redemption.RedemptionRequestDto;
import com.beta.loyalty.dto.venue.AssignStaffRequest;
import com.beta.loyalty.dto.venue.StaffVenueResponse;
import com.beta.loyalty.dto.venue.UpdateVenueRequest;
import com.beta.loyalty.dto.venue.UpdateVenueStatusRequest;
import com.beta.loyalty.dto.venue.VenueAssignmentResponse;
import com.beta.loyalty.dto.venue.VenueCreateRequest;
import com.beta.loyalty.security.AuthPrincipal;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.receipt.StaffReceiptService;
import com.beta.loyalty.service.redemption.StaffRedemptionService;
import com.beta.loyalty.service.venue.StaffVenueService;
import com.beta.loyalty.service.venue.VenueAssignmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff/venues")
@Tag(name = "Staff Venues", description = "Venue management for staff")
public class StaffVenueController {
    private final StaffVenueService staffVenueService;
    private final VenueAssignmentService venueAssignmentService;
    private final StaffRedemptionService staffRedemptionService;
    private final StaffReceiptService staffReceiptService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffVenueResponse createVenue(@Valid @RequestBody VenueCreateRequest req) {
        return staffVenueService.createVenue(currentTenantId(), req);
    }

    @GetMapping
    public Page<StaffVenueResponse> listVenues(Pageable pageable) {
        return staffVenueService.listVenues(currentTenantId(), pageable);
    }

    @GetMapping("/{id}")
    public StaffVenueResponse getVenue(@PathVariable UUID id) {
        return staffVenueService.getVenue(currentTenantId(), id);
    }

    @PatchMapping("/{id}")
    public StaffVenueResponse updateVenue(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVenueRequest req) {

        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffVenueService.updateVenue(p.userId(), p.tenantId(), id, req);
    }

    @PatchMapping("/{id}/status")
    public StaffVenueResponse updateVenueStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVenueStatusRequest req) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffVenueService.updateVenueStatus(p.userId(), p.tenantId(), id, req);
    }

    // --- Assignments ---

    @PostMapping("/{venueId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public VenueAssignmentResponse assignStaff(
            @PathVariable UUID venueId,
            @Valid @RequestBody AssignStaffRequest req) {
        return venueAssignmentService.assign(currentTenantId(), venueId, req);
    }

    @GetMapping("/{venueId}/assignments")
    public List<VenueAssignmentResponse> listAssignments(@PathVariable UUID venueId) {
        return venueAssignmentService.listAssignments(currentTenantId(), venueId);
    }

    @DeleteMapping("/{venueId}/assignments/{staffId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeAssignment(
            @PathVariable UUID venueId,
            @PathVariable UUID staffId) {
        venueAssignmentService.removeAssignment(currentTenantId(), venueId, staffId);
    }

    // --- Receipts ---

    @GetMapping("/{venueId}/receipts")
    public Page<ReceiptResponse> listReceipts(
            @PathVariable UUID venueId,
            Pageable pageable) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffReceiptService.listReceipts(p.userId(), p.tenantId(), venueId, pageable);
    }

    // --- Redemption history ---

    @GetMapping("/{venueId}/redemptions")
    public Page<RedemptionRequestDto> listRedemptions(
            @PathVariable UUID venueId,
            @RequestParam(required = false) RedemptionStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
            Pageable pageable) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffRedemptionService.getHistory(p.userId(), p.tenantId(), venueId, status, from, to, pageable);
    }

    @GetMapping("/{venueId}/redemptions/{redemptionId}")
    public RedemptionRequestDto getRedemption(
            @PathVariable UUID venueId,
            @PathVariable UUID redemptionId) {
        AuthPrincipal p = CurrentUser.requirePrincipal();
        return staffRedemptionService.getOne(p.userId(), p.tenantId(), venueId, redemptionId);
    }

    private UUID currentTenantId() {
        return CurrentUser.requirePrincipal().tenantId();
    }
}
