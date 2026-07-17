package com.duyntb.ticketflow.security.filter;

import com.duyntb.ticketflow.security.SecurityResponseWriter;
import com.duyntb.ticketflow.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/// Purpose:
/// - Runs after JwtAuthenticationFilter (authentication already resolved)
/// - Blocks access to any endpoint other than a small whitelist
///   when the authenticated user still has a temporary password
@Component
@RequiredArgsConstructor
public class PasswordChangeRequiredFilter extends OncePerRequestFilter {
    private final SecurityResponseWriter securityResponseWriter;

    private static final Set<String> ALLOWED_PATHS = Set.of(
            "/api/users/me/password",
            "/api/auth/logout"
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && auth.getPrincipal() instanceof User user) {
            boolean isWhitelisted = ALLOWED_PATHS.contains(request.getRequestURI());
            if (user.isMustChangePassword() && !isWhitelisted) {
                securityResponseWriter.writeError(
                        response,
                        HttpServletResponse.SC_FORBIDDEN,
                        "You need to change your password before continuing"
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
