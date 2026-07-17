package com.duyntb.ticketflow.auth.dto;

import com.duyntb.ticketflow.user.dto.UserResponse;

public record LoginResponse (
        String accessToken,
        String refreshToken,
        Boolean mustChangePassword,
        UserResponse user
) {
    public static LoginResponse from(String accessToken, String refreshToken, boolean mustChangePassword, UserResponse user) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                mustChangePassword,
                user
        );
    }
}
