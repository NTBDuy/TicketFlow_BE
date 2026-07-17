package com.duyntb.ticketflow.security;

import com.duyntb.ticketflow.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SecurityUtils {
    public User getCurrentUser() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof User user)) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        return user;
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    public String getJti() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        Object jti = request.getAttribute("jti");
        if (!(jti instanceof String tokenId) || tokenId.isBlank()) {
            throw new AuthenticationCredentialsNotFoundException("Token id not found");
        }

        return tokenId;
    }
}