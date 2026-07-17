package com.duyntb.ticketflow.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @NotBlank(message = "New password is required")
        @Size(
                min = 8,
                max = 128,
                message = "New password must contain between 8 and 128 characters"

        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "New password must contain uppercase, lowercase, number and special character"

        )
        String newPassword
) {
}