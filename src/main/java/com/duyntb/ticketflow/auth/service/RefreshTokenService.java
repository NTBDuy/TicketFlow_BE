package com.duyntb.ticketflow.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final Duration REFRESH_TOKEN_EXPIRATION = Duration.ofDays(7);

    private final StringRedisTemplate redisTemplate;

    public void save(String jti) {
        String key = buildRefreshTokenKey(jti);
        redisTemplate.opsForValue().set(key, "valid", REFRESH_TOKEN_EXPIRATION);
    }

    public boolean isValid(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildRefreshTokenKey(jti)));
    }

    public void revoke(String jti) {
        redisTemplate.delete(buildRefreshTokenKey(jti));
    }

    private String buildRefreshTokenKey(String jti) {
        return REFRESH_TOKEN_PREFIX + jti;
    }


}
