package com.duyntb.ticketflow.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @Schema(
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
