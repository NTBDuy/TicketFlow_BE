package com.duyntb.ticketflow.common.utils;

import java.security.SecureRandom;

public class OTPGenerator {
    private static final int DEFAULT_OTP_LENGTH = 6;
    private static final int MIN_OTP_LENGTH = 6;
    private static final String DIGIT = "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private OTPGenerator() {}

    public static String generateOTP() {
        return generateOTP(DEFAULT_OTP_LENGTH);
    }

    public static String generateOTP(int length) {
        if (length < MIN_OTP_LENGTH) {
            throw new IllegalArgumentException("OTP length must be at least " + MIN_OTP_LENGTH);
        }

        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            stringBuilder.append(pick());
        }

        return  stringBuilder.toString();
    }

    private static char pick() {
        return DIGIT.charAt(RANDOM.nextInt(DIGIT.length()));
    }
}
