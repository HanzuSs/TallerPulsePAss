package com.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey key;
    private final String issuer;
    private final long expirationSeconds;

    public JwtService(@Value("${pulsepass.jwt.secret}") String secret,
                      @Value("${pulsepass.jwt.issuer:pulsepass-api}") String issuer,
                      @Value("${pulsepass.jwt.expiration-seconds:900}") long expirationSeconds) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("pulsepass.jwt.secret must contain at least 32 UTF-8 bytes");
        }
        if (expirationSeconds < 1) throw new IllegalArgumentException("JWT expiration must be positive");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder().subject(userId.toString()).issuer(issuer)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationSeconds)))
                .id(UUID.randomUUID().toString()).signWith(key, Jwts.SIG.HS256).compact();
    }

    public Claims parseAndValidate(String token) {
        var parsed = Jwts.parser().verifyWith(key).requireIssuer(issuer).build().parseSignedClaims(token);
        if (!"HS256".equals(parsed.getHeader().getAlgorithm())) {
            throw new io.jsonwebtoken.JwtException("Unsupported JWT algorithm");
        }
        Claims claims = parsed.getPayload();
        if (claims.getSubject() == null || claims.getSubject().isBlank() || claims.getExpiration() == null) {
            throw new io.jsonwebtoken.JwtException("Required JWT claims are missing");
        }
        return claims;
    }
    public long getExpirationSeconds() { return expirationSeconds; }
}
