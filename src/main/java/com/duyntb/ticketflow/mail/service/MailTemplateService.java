package com.duyntb.ticketflow.mail.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class MailTemplateService {
    private final TemplateEngine templateEngine;

    public String buildAccountCreatedByAdminEmail(
            String fullName,
            String email,
            String tempPassword
    ) {
        Context context = new Context();

        context.setVariable("fullName", fullName);
        context.setVariable("email", email);
        context.setVariable("temporaryPassword", tempPassword);

        return templateEngine.process("mail/account-created-by-admin", context);
    }

    public String buildResetPasswordByAdminEmail(
            String fullName,
            String email,
            String tempPassword
    ) {
        Context context = new Context();

        context.setVariable("fullName", fullName);
        context.setVariable("email", email);
        context.setVariable("temporaryPassword", tempPassword);

        return templateEngine.process("mail/password-reset-by-admin", context);
    }

    public String buildOtpConfirmMail(
            String fullName,
            String otp,
            long expirationMinutes
    ) {
        Context context = new Context();

        context.setVariable("fullName", fullName);
        context.setVariable("otp", otp);
        context.setVariable("expirationMinutes", expirationMinutes);

        return templateEngine.process("mail/password-reset", context);
    }

    public String buildOtpVerifyMail(
            String fullName,
            String otp,
            long expirationMinutes
    ) {
        Context context = new Context();

        context.setVariable("fullName", fullName);
        context.setVariable("otp", otp);
        context.setVariable("expirationMinutes", expirationMinutes);

        return templateEngine.process("mail/verify-email", context);
    }
}
