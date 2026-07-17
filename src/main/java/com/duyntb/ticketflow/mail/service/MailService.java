package com.duyntb.ticketflow.mail.service;

import com.duyntb.ticketflow.mail.dto.SendMailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromMail;

    public void sendSimpleMail(SendMailRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromMail);
            message.setTo(request.to());
            message.setSubject(request.subject());
            message.setText(request.content());

            mailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException(
                    "Cannot send email: " + e.getMessage(), e
            );
        }
    }

    @Async("mailTaskExecutor")
    public void sendHtmlMail(SendMailRequest request) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(fromMail);
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(request.content(), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Cannot create email HTML" ,e);
        }
    }
}
