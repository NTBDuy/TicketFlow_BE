package com.duyntb.ticketflow.user.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateUserRoleRequest (
        @NotNull(message = "Roles is required")
        Set<String> roles
) {
}
