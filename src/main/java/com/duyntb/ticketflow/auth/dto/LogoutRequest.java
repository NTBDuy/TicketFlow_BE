package com.duyntb.ticketflow.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutRequest(
        @Schema(
                description = "The refresh token provided during login to invalidate the session",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String refreshToken
) {}