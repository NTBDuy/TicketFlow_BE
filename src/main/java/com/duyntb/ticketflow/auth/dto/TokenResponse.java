package com.duyntb.ticketflow.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse from(String accessToken, String refreshToken) {
        return new TokenResponse(
                accessToken,
                refreshToken
        );
    }
}
