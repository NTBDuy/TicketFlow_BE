package com.duyntb.ticketflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email is not valid")
        @Size(
                max = 254,
                message = "Email must not exceed 254 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                min = 8,
                max = 128,
                message = "Password must contain between 8 and 128 characters"

        )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                message = "Password must contain uppercase, lowercase, number and special character"

        )
        String password,

        @NotBlank(message = "Full name is required")
        @Size(
                min = 2,
                max = 150,
                message = "Full name must be between 2 and 150 characters")
        String fullName
) {
        public RegisterRequest {
                email    = email    != null ? email.strip().toLowerCase() : null;
                fullName = fullName != null ? fullName.strip() : null;
        }
}
