package com.duyntb.ticketflow.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record UpdateUserRoleRequest (
        @Schema(
                description = "The set of role names to assign to the user",
                example = "[\"USER\", \"ADMIN\"]"
        )
        @NotNull(message = "Roles is required")
        Set<String> roles
) {
}