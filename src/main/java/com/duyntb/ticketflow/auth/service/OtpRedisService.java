package com.duyntb.ticketflow.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpRedisService {

    public enum OtpPurpose {
        FORGOT_PASSWORD("forgot-password"),
        EMAIL_VERIFICATION("email-verification");

        private final String prefix;
        OtpPurpose(String prefix) { this.prefix = prefix; }
    }

    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(5);
    private static final Duration SEND_COOLDOWN_EXPIRATION = Duration.ofMinutes(1);
    private static final Duration SEND_COUNT_EXPIRATION = Duration.ofHours(1);

    private static final int MAX_OTP_VERIFY_ATTEMPTS = 5;
    private static final int MAX_OTP_SENDS_PER_HOUR = 5;

    private final StringRedisTemplate redisTemplate;

    public long getOtpExpirationMinutes() {
        return OTP_EXPIRATION.toMinutes();
    }

    public boolean tryStartCooldown(OtpPurpose purpose, String email) {
        String key = buildKey(purpose, "cooldown", email);
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", SEND_COOLDOWN_EXPIRATION);
        return Boolean.TRUE.equals(success);
    }

    public boolean tryConsumeHourlySend(OtpPurpose purpose, String email) {
        String key = buildKey(purpose, "send-count", email);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == 1) {
            redisTemplate.expire(key, SEND_COUNT_EXPIRATION);
        }
        return count <= MAX_OTP_SENDS_PER_HOUR;
    }

    public void saveOtp(OtpPurpose purpose, String email, String otp) {
        redisTemplate.opsForValue().set(buildKey(purpose, "otp", email), otp, OTP_EXPIRATION);
        deleteOtpAttempts(purpose, email);
    }

    public void deleteOtp(OtpPurpose purpose, String email) {
        redisTemplate.delete(buildKey(purpose, "otp", email));
        deleteOtpAttempts(purpose, email);
    }

    public boolean isInvalidOtp(OtpPurpose purpose, String email, String inputOtp) {
        String attemptsKey = buildKey(purpose, "otp-attempts", email);
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts == 1) {
            redisTemplate.expire(attemptsKey, OTP_EXPIRATION);
        }
        if (attempts > MAX_OTP_VERIFY_ATTEMPTS) {
            deleteOtp(purpose, email);
            return true;
        }

        String savedOtp = redisTemplate.opsForValue().get(buildKey(purpose, "otp", email));
        if (savedOtp == null || !savedOtp.equals(inputOtp)) {
            return true;
        }

        deleteOtp(purpose, email);
        return false;
    }

    private void deleteOtpAttempts(OtpPurpose purpose, String email) {
        redisTemplate.delete(buildKey(purpose, "otp-attempts", email));
    }

    private String buildKey(OtpPurpose purpose, String segment, String email) {
        return purpose.prefix + ":" + segment + ":" + email.toLowerCase();
    }
}