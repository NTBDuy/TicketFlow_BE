package com.duyntb.ticketflow.auth.controller;

import com.duyntb.ticketflow.auth.dto.*;
import com.duyntb.ticketflow.auth.service.AuthService;
import com.duyntb.ticketflow.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentication",
        description = "APIs for authentication, password recovery, and email verification"
)
public class AuthController {
    private final AuthService authService;

    // ---------------------------------------------------------------------
    // AUTHENTICATION
    // ---------------------------------------------------------------------

    @Operation(
            summary = "Register a new account",
            description = """
                    Register a new user account.
                    If the email is valid, a verification OTP will be sent to the user's email.
                    """
    )
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("If your email is valid, a verification code has been sent.", null);
    }

    @Operation(
            summary = "Login",
            description = "Authenticate a user using their credentials and return access and refresh tokens."
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successfully.", authService.login(request));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generate a new access token using a valid refresh token."
    )
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success("Refresh successfully.", authService.refresh(request));
    }

    @Operation(
            summary = "Logout",
            description = "Logout the current user and revoke the provided token."
    )
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.success("Logged out successfully.", null);
    }

    // ---------------------------------------------------------------------
    // FORGOT PASSWORD
    // ---------------------------------------------------------------------

    @Operation(
            summary = "Request password reset",
            description = """
                    Request a password reset OTP.
                    If an account exists with the provided email,
                    a verification code will be sent to that email.
                    """
    )
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ConfirmEmailRequest request) {
        authService.requestPasswordReset(request);
        return ApiResponse.success(
                "If an account exists for this email, a verification code has been sent.",
                null
        );
    }

    @Operation(
            summary = "Verify password reset OTP",
            description = """
                    Verify the OTP sent for password recovery.
                    Returns a reset token that can be used to reset the password.
                    """
    )
    @PostMapping("/confirm-otp")
    public ApiResponse<String> confirmOtp(@Valid @RequestBody ConfirmOtpRequest request) {
        return ApiResponse.success(
                "Confirm OTP successfully.",
                authService.verifyPasswordResetOtp(request)
        );
    }

    @Operation(
            summary = "Reset password",
            description = "Reset the user's password using a valid password reset token."
    )
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("Reset Password successfully", null);
    }

    // ---------------------------------------------------------------------
    // EMAIL VERIFICATION
    // ---------------------------------------------------------------------

    @Operation(
            summary = "Verify email",
            description = "Verify a user's email address using the OTP sent during registration."
    )
    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody ConfirmOtpRequest request) {
        authService.verifyEmailOtp(request);
        return ApiResponse.success(
                "Confirm OTP successfully.", null
        );
    }

    @Operation(
            summary = "Resend email verification OTP",
            description = "Send a new email verification OTP to the provided email address."
    )
    @PostMapping("/resend-verification-otp")
    public ApiResponse<Void> resendVerificationOtp(@Valid @RequestBody ConfirmEmailRequest request) {
        authService.resendVerificationOtp(request);
        return ApiResponse.success("Resend Verification OTP successfully.", null);
    }
}
