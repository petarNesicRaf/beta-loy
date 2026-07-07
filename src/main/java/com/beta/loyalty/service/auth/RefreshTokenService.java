package com.beta.loyalty.service.auth;

import com.beta.loyalty.domain.RefreshToken;
import com.beta.loyalty.dto.auth.TokenResponse;
import com.beta.loyalty.exception.UnauthorizedException;
import com.beta.loyalty.repository.auth.RefreshTokenRepository;
import com.beta.loyalty.security.AuthPrincipal;
import com.beta.loyalty.security.UserType;
import com.beta.loyalty.security.jwt.JwtProperties;
import com.beta.loyalty.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProps;

    @Transactional
    public RefreshToken create(UUID userId, UserType userType, UUID tenantId, List<String> roles) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(UUID.randomUUID());
        rt.setUserId(userId);
        rt.setUserType(userType);
        rt.setTenantId(tenantId);
        rt.setRoles(String.join(",", roles));
        rt.setExpiresAt(OffsetDateTime.now().plusSeconds(jwtProps.refreshTokenSeconds()));
        rt.setRevoked(false);
        return refreshTokenRepository.save(rt);
    }

    @Transactional
    public TokenResponse rotate(String rawToken) {
        UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(rawToken);
        } catch (IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        RefreshToken rt = refreshTokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

        if (rt.isRevoked()) {
            throw new UnauthorizedException("Refresh token has been revoked");
        }
        if (rt.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException("Refresh token has expired");
        }

        // Revoke the used token (rotation — prevents replay)
        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        // Issue a fresh refresh token
        List<String> roles = Arrays.asList(rt.getRoles().split(","));
        RefreshToken newRt = create(rt.getUserId(), rt.getUserType(), rt.getTenantId(), roles);

        // Issue a new access token
        AuthPrincipal principal = new AuthPrincipal(rt.getUserId(), rt.getUserType(), rt.getTenantId());
        String accessToken = jwtService.mintAccessToken(principal, roles);

        return new TokenResponse(
                accessToken,
                "Bearer",
                jwtProps.accessTokenSeconds(),
                rt.getUserId(),
                rt.getUserType().name(),
                rt.getTenantId(),
                roles,
                newRt.getToken().toString(),
                jwtProps.refreshTokenSeconds()
        );
    }

    @Transactional
    public void revokeAll(UUID userId) {
        refreshTokenRepository.deleteAllByUserId(userId);
    }
}
