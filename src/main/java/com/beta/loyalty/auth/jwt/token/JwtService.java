package com.beta.loyalty.auth.jwt.token;

import com.beta.loyalty.auth.AuthPrincipal;
import com.beta.loyalty.auth.UserType;
import com.beta.loyalty.auth.jwt.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;

public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final long accessTokenSeconds;

    public JwtService(JwtProperties props){
        this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        this.issuer = props.issuer();
        this.accessTokenSeconds = props.accessTokenSeconds();
    }

    public String mintAccessToken(AuthPrincipal principal, List<String> roles) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(accessTokenSeconds);

        JwtBuilder builder = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(principal.userId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .claim("typ", principal.userType().name())
                .claim("roles", roles);

        if (principal.userType() == UserType.STAFF && principal.tenantId() != null) {
            builder.claim("tenantId", principal.tenantId().toString());
        }

        return builder.signWith(key).compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
