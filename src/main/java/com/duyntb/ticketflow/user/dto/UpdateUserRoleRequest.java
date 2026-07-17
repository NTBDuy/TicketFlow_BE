package com.duyntb.ticketflow.user.dto;

import com.duyntb.ticketflow.user.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest (
        @NotNull(message = "Role is required")
        Role role
) {
}
