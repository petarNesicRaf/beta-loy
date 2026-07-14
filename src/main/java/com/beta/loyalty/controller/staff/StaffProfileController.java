package com.beta.loyalty.controller.staff;

import com.beta.loyalty.dto.staff.ChangePasswordRequest;
import com.beta.loyalty.dto.staff.StaffProfileResponse;
import com.beta.loyalty.security.CurrentUser;
import com.beta.loyalty.service.staff.StaffProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/staff/me")
@Tag(name = "Staff - Profile", description = "Own profile and password management")
public class StaffProfileController {

    private final StaffProfileService staffProfileService;

    @GetMapping
    public StaffProfileResponse getProfile() {
        return staffProfileService.getProfile(CurrentUser.requirePrincipal().userId());
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        staffProfileService.changePassword(CurrentUser.requirePrincipal().userId(), req);
    }
}
