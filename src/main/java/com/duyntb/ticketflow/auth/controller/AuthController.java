package com.duyntb.ticketflow.auth.controller;

import com.duyntb.ticketflow.auth.dto.*;
import com.duyntb.ticketflow.auth.service.AuthService;
import com.duyntb.ticketflow.common.response.ApiResponse;
import com.duyntb.ticketflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("If your email is valid, a verification code has been sent.", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successfully.", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Refresh successfully.", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.success("Logged out successfully.", null);
    }

    // ---------------------------------------------------------------------
    // FORGOT PASSWORD
    // ---------------------------------------------------------------------

    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ConfirmEmailRequest request) {
        authService.requestPasswordReset(request);
        return ApiResponse.success(
                "If an account exists for this email, a verification code has been sent.",
                null
        );
    }

    @PostMapping("/confirm-otp")
    public ApiResponse<String> confirmOtp(@Valid @RequestBody ConfirmOtpRequest request) {
        return ApiResponse.success(
                "Confirm OTP successfully.",
                authService.verifyPasswordResetOtp(request)
        );
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Reset Password successfully", null);
    }

    // ---------------------------------------------------------------------
    // EMAIL VERIFICATION
    // ---------------------------------------------------------------------

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody ConfirmOtpRequest request) {
        authService.verifyEmailOtp(request);
        return ApiResponse.success(
                "Confirm OTP successfully.", null
        );
    }

    @PostMapping("/resend-verification-otp")
    public ApiResponse<Void> resendVerificationOtp(@Valid @RequestBody ConfirmEmailRequest request) {
        authService.resendVerificationOtp(request);
        return ApiResponse.success("Resend Verification OTP successfully.", null);
    }
}
