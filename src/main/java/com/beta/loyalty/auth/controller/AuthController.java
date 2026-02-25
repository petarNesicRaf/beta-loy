package com.beta.loyalty.auth.controller;

import com.beta.loyalty.auth.jwt.dto.LoginRequest;
import com.beta.loyalty.auth.TokenResponse;
import com.beta.loyalty.auth.oauth.google.GoogleLoginRequest;
import com.beta.loyalty.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Tag(
        name = "Auth",
        description = "Google login, customer login, staff login"
)
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public TokenResponse google(@RequestBody GoogleLoginRequest req) {
        return authService.customerGoogleLogin(req);
    }

    @PostMapping("/customer/login")
    public TokenResponse customerLogin(@RequestBody LoginRequest request) {
        return authService.customerLogin(request);
    }

    @PostMapping("/staff/login")
    public TokenResponse staffLogin(@RequestBody LoginRequest request) {
        return authService.staffLogin(request);
    }
}
