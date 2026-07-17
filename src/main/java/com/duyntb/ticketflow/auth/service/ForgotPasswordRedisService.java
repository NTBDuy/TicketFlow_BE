package com.duyntb.ticketflow.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ForgotPasswordRedisService {
    private static final String TOKEN_KEY_PREFIX = "reset-token:";
    private static final Duration RESET_TOKEN_EXPIRATION = Duration.ofMinutes(10);
    private final StringRedisTemplate redisTemplate;

    public void saveToken(String token, Long userId) {
        redisTemplate.opsForValue().set(buildTokenKey(token), userId.toString(), RESET_TOKEN_EXPIRATION);
    }

    public Optional<Long> getUserIdFromToken(String token) {
        String userId = redisTemplate.opsForValue().get(buildTokenKey(token));
        return Optional.ofNullable(userId).map(Long::valueOf);
    }

    public void deleteToken(String token) {
        redisTemplate.delete(buildTokenKey(token));
    }

    private String buildTokenKey(String token) {
        return TOKEN_KEY_PREFIX + token;
    }
}
