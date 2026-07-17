package com.duyntb.ticketflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 150, message = "Full name must be between 2 and 150 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email
) {
        public UpdateUserRequest {
                fullName = fullName != null ? fullName.strip() : null;
                email    = email    != null ? email.strip().toLowerCase() : null;
        }
}