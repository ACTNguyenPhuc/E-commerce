package com.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    public static final String TOKEN_TYPE = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_EMAIL = "email";

    private final JwtProperties props;

    private SecretKey signingKey() {
        byte[] bytes;
        try {
            bytes = Decoders.BASE64.decode(props.getSecret());
            if (bytes.length < 32) throw new IllegalArgumentException("too short");
        } catch (Exception ex) {
            bytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(bytes.length >= 32 ? bytes : padTo32(bytes));
    }

    private byte[] padTo32(byte[] src) {
        byte[] dest = new byte[32];
        System.arraycopy(src, 0, dest, 0, Math.min(src.length, 32));
        return dest;
    }

    public String generateAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getAccessTtlMinutes(), ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(props.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(TOKEN_TYPE, TYPE_ACCESS, CLAIM_EMAIL, email, CLAIM_ROLE, role))
                .signWith(signingKey())
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant exp = now.plus(props.getRefreshTtlDays(), ChronoUnit.DAYS);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(props.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .claims(Map.of(TOKEN_TYPE, TYPE_REFRESH))
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .requireIssuer(props.getIssuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public long getAccessTtlSeconds() {
        return props.getAccessTtlMinutes() * 60;
    }
}
