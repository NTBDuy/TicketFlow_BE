package com.duyntb.ticketflow.user.dto;

import com.duyntb.ticketflow.user.entity.Role;
import com.duyntb.ticketflow.user.entity.User;

import java.util.stream.Collectors;

public record UserResponse (
        Long id,
        String fullName,
        String email,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(", "))
        );
    }
}
