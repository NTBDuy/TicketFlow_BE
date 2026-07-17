package com.duyntb.ticketflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest (
        @Email(message = "Email is not valid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Password must contain at least 8 characters, uppercase, lowercase, number and special character"
        )
        String password
) {
}
