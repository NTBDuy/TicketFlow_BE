package com.duyntb.ticketflow.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmOtpRequest(
        @Email(message = "Email is not valid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @Schema(
                example = "123456"
        )
        @NotBlank(message = "OTP is required")
        String otp
) {
}
