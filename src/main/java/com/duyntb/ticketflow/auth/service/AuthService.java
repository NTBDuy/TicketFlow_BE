package com.duyntb.ticketflow.auth.service;

import com.duyntb.ticketflow.auth.dto.*;
import com.duyntb.ticketflow.auth.service.OtpRedisService.OtpPurpose;
import com.duyntb.ticketflow.common.exception.BadRequestException;
import com.duyntb.ticketflow.common.exception.ResourceNotFoundException;
import com.duyntb.ticketflow.common.utils.OTPGenerator;
import com.duyntb.ticketflow.mail.dto.SendMailRequest;
import com.duyntb.ticketflow.mail.service.MailService;
import com.duyntb.ticketflow.mail.service.MailTemplateService;
import com.duyntb.ticketflow.security.JwtService;
import com.duyntb.ticketflow.security.SecurityUtils;
import com.duyntb.ticketflow.user.dto.UserResponse;
import com.duyntb.ticketflow.user.entity.Role;
import com.duyntb.ticketflow.user.entity.User;
import com.duyntb.ticketflow.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ForgotPasswordRedisService forgotPasswordRedisService;
    private final RedisTokenRevocationService tokenRedisService;
    private final OtpRedisService otpRedisService;
    private final MailService mailService;
    private final MailTemplateService mailTemplateService;
    private final SecurityUtils securityUtils;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void register(RegisterRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.email());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (!user.isEnabled()) {
                otpMailSender(OtpPurpose.EMAIL_VERIFICATION, user, this::sendEmailVerificationOtpMail);
            }
            return;
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .enabled(false)
                .build();

        userRepository.save(user);

        otpMailSender(OtpPurpose.EMAIL_VERIFICATION, user, this::sendEmailVerificationOtpMail);
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("Authenticated principal is not a User");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        String jti = jwtService.parseRefreshToken(refreshToken).getId();
        refreshTokenService.save(jti);

        return LoginResponse.from(accessToken, refreshToken, user.isMustChangePassword(), UserResponse.from(user));
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        Claims claims = jwtService.parseRefreshToken(request.refreshToken());
        if (!refreshTokenService.isValid(claims.getId())) {
            throw new BadRequestException("Refresh token is invalid or revoked");
        }

        User user = userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        refreshTokenService.revoke(claims.getId());

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        String jti = jwtService.parseRefreshToken(newRefreshToken).getId();
        refreshTokenService.save(jti);

        return TokenResponse.from(newAccessToken, newRefreshToken);
    }

    public void logout(LogoutRequest request) {
        tokenRedisService.revokeToken(securityUtils.getJti());

        try {
            Claims claims = jwtService.parseRefreshToken(request.refreshToken());
            refreshTokenService.revoke(claims.getId()); // Revoke refresh token
        } catch (JwtException e) {
            // If refresh token is invalid, ignore it, don't throw.
        }
    }

    // ---------------------------------------------------------------------
    // FORGOT PASSWORD
    // ---------------------------------------------------------------------

    public void requestPasswordReset(ConfirmEmailRequest request) {
        userRepository.findByEmail(request.email())
                .ifPresent(
                        user -> {
                            otpMailSender(OtpPurpose.FORGOT_PASSWORD, user, this::sendForgotPasswordOtpMail);
                        }
                );
    }

    public String verifyPasswordResetOtp(ConfirmOtpRequest request) {
        if (otpRedisService.isInvalidOtp(OtpPurpose.FORGOT_PASSWORD, request.email(), request.otp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String resetToken = UUID.randomUUID().toString();
        forgotPasswordRedisService.saveToken(resetToken, user.getId());

        return resetToken;
    }

    public void resetPassword(ResetPasswordRequest request) {
        Long userId = forgotPasswordRedisService.getUserIdFromToken(request.resetToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        forgotPasswordRedisService.deleteToken(request.resetToken());
    }

    // ---------------------------------------------------------------------
    // EMAIL VERIFICATION
    // ---------------------------------------------------------------------

    public void verifyEmailOtp(ConfirmOtpRequest request) {
        if (otpRedisService.isInvalidOtp(OtpPurpose.EMAIL_VERIFICATION, request.email(), request.otp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);
    }

    public void resendVerificationOtp(ConfirmEmailRequest request) {
        userRepository.findByEmail(request.email())
                .filter(User::isDisabled)
                .ifPresent(
                        user -> otpMailSender(OtpPurpose.EMAIL_VERIFICATION, user, this::sendEmailVerificationOtpMail)
                );
    }

    // ---------------------------------------------------------------------
    // PRIVATE HELPER
    // ---------------------------------------------------------------------

    private void otpMailSender(OtpPurpose purpose, User user, BiConsumer<User, String> mailSender) {
        /// Cooldown 1 request / 60s
        if (!otpRedisService.tryStartCooldown(purpose, user.getEmail())) {
            return;
        }

        /// Rate limit 5 request / hour
        if (!otpRedisService.tryConsumeHourlySend(purpose, user.getEmail())) {
            return;
        }
        String otp = OTPGenerator.generateOTP();
        otpRedisService.saveOtp(purpose, user.getEmail(), otp);
        mailSender.accept(user, otp);
    }

    private void sendForgotPasswordOtpMail(User user, String otp) {
        String htmlContent = mailTemplateService.buildOtpConfirmMail(
                user.getFullName(),
                otp,
                otpRedisService.getOtpExpirationMinutes()
        );

        SendMailRequest request = new SendMailRequest(
                user.getEmail(),
                "[Ticket Flow] Confirm Email",
                htmlContent
        );

        mailService.sendHtmlMail(request);
    }

    private void sendEmailVerificationOtpMail(User user, String otp) {
        String htmlContent = mailTemplateService.buildOtpVerifyMail(
                user.getFullName(),
                otp,
                otpRedisService.getOtpExpirationMinutes()
        );

        SendMailRequest request = new SendMailRequest(
                user.getEmail(),
                "[Ticket Flow] Verify Email",
                htmlContent
        );

        mailService.sendHtmlMail(request);
    }
}
