package com.duyntb.ticketflow.user.controller;

import com.duyntb.ticketflow.common.response.ApiResponse;
import com.duyntb.ticketflow.common.response.PageResponse;
import com.duyntb.ticketflow.user.dto.*;
import com.duyntb.ticketflow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(
        name = "User Management",
        description = "APIs for managing users"
)
@SecurityRequirement(name = "bearerAuth")
public class  UserController {
    private final UserService service;

    @Operation(
            summary = "Get users",
            description = "Retrieve a paginated list of users with optional keyword search."
    )
    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return ApiResponse.success(service.getUsers(keyword, page, size));
    }

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve detailed information of a user by their ID."
    )
    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(service.getUserById(id));
    }

    @Operation(
            summary = "Create a new user",
            description = "Create a new user account."
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @Operation(
            summary = "Update user",
            description = "Update information of an existing user."
    )
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success("User updated successfully", service.updateUser(id, request));
    }

    @Operation(
            summary = "Delete user",
            description = "Delete a user by their ID."
    )
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ApiResponse.success("User deleted successfully", null);
    }

    @Operation(
            summary = "Change current user's password",
            description = "Change the password of the currently authenticated user."
    )
    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody ChangePasswordRequest request) {
        service.updatePassword(request);
        return ApiResponse.success("Password updated successfully", null);
    }

    @Operation(
            summary = "Reset user password",
            description = "Reset the password of a specific user."
    )
    @PatchMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return ApiResponse.success("Password reset successfully", null);
    }

    @Operation(
            summary = "Update user role",
            description = "Assign or update the role of a specific user."
    )
    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        service.updateUserRole(id, request);
        return ApiResponse.success("Role updated successfully", null);
    }

    @Operation(
            summary = "Toggle user lock status",
            description = "Lock or unlock a specific user account depending on its current status."
    )
    @PatchMapping("/{id}/toggle-lock")
    public ApiResponse<Void> lockUser(@PathVariable Long id) {
        service.toggleUserLock(id);
        return ApiResponse.success("User lock status updated successfully", null);
    }
}