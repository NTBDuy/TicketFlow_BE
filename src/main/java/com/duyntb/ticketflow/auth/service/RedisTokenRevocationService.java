package com.duyntb.ticketflow.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisTokenRevocationService {
    private static final String TOKEN_REVOKED_BY_USER_KEY_PREFIX = "token-revoked-before:";
    private static final String TOKEN_REVOKED_KEY_PREFIX = "token-revoked:";
    private static final Duration TOKEN_REVOKED_EXPIRATION = Duration.ofHours(1);

    private final StringRedisTemplate redisTemplate;

    public void revokeToken(String jti) {
        String key = buildTokenRevokedKey(jti);
        redisTemplate.opsForValue().set(
                key, "1", TOKEN_REVOKED_EXPIRATION
        );
    }

    public void revokeAllTokensForUser(Long userId) {
        String key = buildTokenRevokedByUserKey(userId);
        redisTemplate.opsForValue().set(
                key,
                String.valueOf(Instant.now().toEpochMilli()),
                TOKEN_REVOKED_EXPIRATION
        );
    }

    public boolean isTokenRevoked(String jti) {
        return redisTemplate.hasKey(buildTokenRevokedKey(jti));
    }

    public boolean isTokenIssuedBeforeRevocation(Long userId, Instant issuedAt) {
        String value = redisTemplate.opsForValue().get(buildTokenRevokedByUserKey(userId));
        if (value == null) {
            return false;
        }

        long revokedAtMillis = Long.parseLong(value);
        return issuedAt.toEpochMilli() < revokedAtMillis;
    }

    private String buildTokenRevokedByUserKey(Long userId) { return TOKEN_REVOKED_BY_USER_KEY_PREFIX + userId; }
    private String buildTokenRevokedKey(String jti) { return TOKEN_REVOKED_KEY_PREFIX + jti; }
}
