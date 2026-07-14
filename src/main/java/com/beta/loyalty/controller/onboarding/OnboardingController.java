package com.beta.loyalty.controller.onboarding;

import com.beta.loyalty.dto.onboarding.OnboardingRequest;
import com.beta.loyalty.dto.onboarding.OnboardingResponse;
import com.beta.loyalty.service.onboarding.OnboardingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/onboarding")
@Tag(name = "Onboarding", description = "Self-service business registration — creates tenant, owner account, and first venue in one step")
public class OnboardingController {

    private final OnboardingService onboardingService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public OnboardingResponse register(@Valid @RequestBody OnboardingRequest req) {
        return onboardingService.register(req);
    }
}
