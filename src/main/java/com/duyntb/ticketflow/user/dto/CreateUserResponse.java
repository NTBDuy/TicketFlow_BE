package com.duyntb.ticketflow.user.dto;

import com.duyntb.ticketflow.user.entity.Role;
import com.duyntb.ticketflow.user.entity.User;

import java.util.stream.Collectors;

public record CreateUserResponse(
        Long id,
        String fullName,
        String email,
        String role,
        String tempPassword
) {
    public static CreateUserResponse from(User user, String tempPassword) {
        return new CreateUserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.joining(", ")),
                tempPassword
        );
    }
}
