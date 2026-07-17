package com.duyntb.ticketflow.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendMailRequest (
        @NotBlank(message = "Recipient email cannot be blank")
        @Email(message = "Recipient email format is invalid")
        String to,

        @NotBlank(message = "Subject cannot be blank")
        String subject,

        @NotBlank(message = "Content cannot be blank")
        String content
) {
}
