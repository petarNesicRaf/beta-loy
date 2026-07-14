package com.beta.loyalty.controller.staff;

import com.beta.loyalty.dto.staff.InviteStaffRequest;
import com.beta.loyalty.dto.staff.StaffMemberResponse;
import com.beta.loyalty.dto.staff.UpdateStaffRolesRequest;
import com.beta.loyalty.dto.staff.UpdateStaffStatusRequest;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.staff.StaffMemberService;
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
@RequestMapping("/api/v1/staff/members")
@Tag(name = "Staff Members", description = "Staff member management within a tenant")
public class StaffMemberController {
    private final StaffMemberService staffMemberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffMemberResponse invite(@Valid @RequestBody InviteStaffRequest req) {
        return staffMemberService.invite(currentTenantId(), req);
    }

    @GetMapping
    public Page<StaffMemberResponse> listMembers(Pageable pageable) {
        return staffMemberService.listMembers(currentTenantId(), pageable);
    }

    @GetMapping("/{id}")
    public StaffMemberResponse getMember(@PathVariable UUID id) {
        return staffMemberService.getMember(currentTenantId(), id);
    }

    @PatchMapping("/{id}/status")
    public StaffMemberResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStaffStatusRequest req) {
        return staffMemberService.updateStatus(currentTenantId(), id, req);
    }

    @PutMapping("/{id}/roles")
    public StaffMemberResponse updateRoles(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStaffRolesRequest req) {
        return staffMemberService.updateRoles(currentTenantId(), id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable UUID id) {
        staffMemberService.deleteMember(currentTenantId(), id);
    }

    private UUID currentTenantId() {
        return CurrentUser.requirePrincipal().tenantId();
    }
}
