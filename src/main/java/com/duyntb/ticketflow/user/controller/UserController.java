package com.duyntb.ticketflow.user.controller;

import com.duyntb.ticketflow.common.response.ApiResponse;
import com.duyntb.ticketflow.common.response.PageResponse;
import com.duyntb.ticketflow.user.dto.*;
import com.duyntb.ticketflow.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class  UserController {
    private final UserService service;

    @GetMapping
    public ApiResponse<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        return ApiResponse.success(service.getUsers(keyword, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUserById(@PathVariable Long id) {
        return ApiResponse.success(service.getUserById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(service.createUser(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.success("User updated successfully", service.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
        return ApiResponse.success("User deleted successfully", null);
    }

    @PatchMapping("/me/password")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody ChangePasswordRequest request) {
        service.updatePassword(request);
        return ApiResponse.success("Password updated successfully", null);
    }

    @PatchMapping("/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id) {
        service.resetPassword(id);
        return ApiResponse.success("Password reset successfully", null);
    }

    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest request) {
        service.updateUserRole(id, request);
        return ApiResponse.success("Role updated successfully", null);
    }

    @PatchMapping("/{id}/toggle-lock")
    public ApiResponse<Void> lockUser(@PathVariable Long id) {
        service.toggleUserLock(id);
        return ApiResponse.success("User lock status updated successfully", null);
    }
}