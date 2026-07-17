package com.duyntb.ticketflow.security;

import com.duyntb.ticketflow.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {
    private final static String ACCESS = "access";
    private final static String REFRESH = "refresh";

    @Value("${jwt.access.secret}")
    private String accessSecretKey;

    @Value("${jwt.refresh.secret}")
    private String refreshSecretKey;

    @Value("${jwt.access.expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;

    private SecretKey accessSigningKey;
    private SecretKey refreshSigningKey;

    @PostConstruct
    private void init() {
        accessSigningKey = Keys.hmacShaKeyFor(accessSecretKey.getBytes(StandardCharsets.UTF_8));
        refreshSigningKey = Keys.hmacShaKeyFor(refreshSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return generateToken(user.getId(), ACCESS, accessExpiration, accessSigningKey);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user.getId(), REFRESH, refreshExpiration, refreshSigningKey);
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseClaims(token, accessSigningKey);
        validateType(claims, ACCESS);
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parseClaims(token, refreshSigningKey);
        validateType(claims, REFRESH);
        return claims;
    }

    ///  Private helper
    private String generateToken(
            Long userId,
            String type,
            long expiration,
            SecretKey signingKey
    ) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey)
                .compact();
    }

    private Claims parseClaims(String token, SecretKey signingKey) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void validateType(Claims claims, String expectedType) {
        if (!expectedType.equals(claims.get("type", String.class))) {
            throw new JwtException("Invalid token type");
        }
    }
}
