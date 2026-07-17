package com.duyntb.ticketflow.security.filter;

import com.duyntb.ticketflow.auth.service.RedisTokenRevocationService;
import com.duyntb.ticketflow.security.JwtService;
import com.duyntb.ticketflow.security.SecurityResponseWriter;
import com.duyntb.ticketflow.user.entity.User;
import com.duyntb.ticketflow.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

/// Purpose:
/// - Reads the Authorization header on each request
/// - If a bearer token is present, validates it and sets Authentication in the SecurityContext
/// - Any invalid/expired/revoked token results in a consistent 401 response
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RedisTokenRevocationService redisService;
    private final SecurityResponseWriter securityResponseWriter;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtService.parseAccessToken(jwt);

            Long userId = Long.parseLong(claims.getSubject());
            Optional<User> optionalUser = userRepository.findByIdWithRolesAndPermissions(userId);
            if (optionalUser.isEmpty()) {
                unauthorized(response, "Invalid token");
                return;
            }

            User user = optionalUser.get();
            String jti = claims.getId();
            Instant issuedAt = claims.getIssuedAt().toInstant();
            if (redisService.isTokenRevoked(jti) ||
                    redisService.isTokenIssuedBeforeRevocation(userId, issuedAt)) {
                unauthorized(response, "Token has been revoked");
                return;
            }

            request.setAttribute("jti", jti);
            authenticate(request, user);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            unauthorized(response, "Invalid or expired token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, User user) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();
        securityResponseWriter.writeError(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}
