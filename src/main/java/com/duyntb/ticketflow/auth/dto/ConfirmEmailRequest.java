package com.duyntb.ticketflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ConfirmEmailRequest(
        @Email(message = "Email is not valid")
        @Size(max = 254, message = "Email must not exceed 254 characters")
        String email
) {
}
