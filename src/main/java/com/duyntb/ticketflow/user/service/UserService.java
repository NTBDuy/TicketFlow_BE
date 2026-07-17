package com.duyntb.ticketflow.user.service;

import com.duyntb.ticketflow.auth.service.RedisTokenRevocationService;
import com.duyntb.ticketflow.common.exception.BadRequestException;
import com.duyntb.ticketflow.common.exception.ResourceNotFoundException;
import com.duyntb.ticketflow.common.response.PageResponse;
import com.duyntb.ticketflow.common.utils.PasswordGenerator;
import com.duyntb.ticketflow.mail.dto.SendMailRequest;
import com.duyntb.ticketflow.mail.service.MailService;
import com.duyntb.ticketflow.mail.service.MailTemplateService;
import com.duyntb.ticketflow.security.SecurityUtils;
import com.duyntb.ticketflow.user.dto.*;
import com.duyntb.ticketflow.user.entity.Role;
import com.duyntb.ticketflow.user.entity.User;
import com.duyntb.ticketflow.user.repository.RoleRepository;
import com.duyntb.ticketflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.duyntb.ticketflow.user.specification.UserSpecifications.hasUser;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final MailService mailService;
    private final MailTemplateService mailTemplateService;
    private final RedisTokenRevocationService tokenRedisService;
    private final RoleRepository roleRepository;

    @PreAuthorize("hasAuthority('USER_READ')")
    public PageResponse<UserResponse> getUsers(String keyword, int page, int size) {
        Specification<User> spec = Specification.where(hasUser(keyword));

        Pageable pageable = PageRequest.of(page, size);

        Page<UserResponse> userPage = userRepository.findAll(spec, pageable)
                .map(UserResponse::from);

        return PageResponse.from(userPage);
    }

    @PreAuthorize("hasAuthority('USER_READ')")
    public UserResponse getUserById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @PreAuthorize("hasAuthority('USER_CREATE')")
    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        String tempPwd = PasswordGenerator.generate(12);
        String pwd = passwordEncoder.encode(tempPwd);

        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() ->
                        new IllegalStateException("Default USER role is not configured"));

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(pwd)
                .mustChangePassword(true)
                .roles(Set.of(defaultRole))
                .build();

        User savedUser = userRepository.save(user);
        sendAccountCreatedByAdminMail(savedUser, tempPwd);
        return CreateUserResponse.from(savedUser, tempPwd);
    }

    @PreAuthorize("hasAuthority('USER_UPDATE')")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new BadRequestException("Email already exists");
        }

        user.setFullName(request.fullName());
        user.setEmail(request.email());

        return UserResponse.from(userRepository.save(user));
    }

    @PreAuthorize("hasAuthority('USER_ROLE_UPDATE')")
    @Transactional
    public void updateUserRole(Long id, UpdateUserRoleRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new BadRequestException("You cannot change your own role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<Role> roles = new HashSet<>(
                roleRepository.findAllByNameIn(request.roles())
        );

        if (roles.size() != request.roles().size()) {
            throw new IllegalStateException("One or more roles do not exist");
        }

        user.setRoles(roles);

        tokenRedisService.revokeAllTokensForUser(id);
    }

    @PreAuthorize("hasAuthority('USER_DELETE')")
    @Transactional
    public void deleteUser(Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId.equals(id))
            throw new BadRequestException("You are not allowed to delete this user");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isDeleted()) {
            throw new ResourceNotFoundException("User is already deleted");
        }

        user.setDeletedAt(LocalDateTime.now());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void updatePassword(ChangePasswordRequest request) {
        User user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password doesn't match");
        }

        if (request.oldPassword().equals(request.newPassword())) {
            throw new BadRequestException("The new password cannot match the old password");
        }

        user.setMustChangePassword(false);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        tokenRedisService.revokeAllTokensForUser(user.getId());
    }

    @PreAuthorize(
            "hasAuthority('USER_PASSWORD_RESET')"
    )
    public void resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String tempPassword = PasswordGenerator.generate(12);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        sendResetPasswordByAdminMail(user, tempPassword);
    }

    @PreAuthorize(
            "hasAuthority('USER_LOCK_UPDATE')"
    )
    @Transactional
    public void toggleUserLock(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setLocked(!user.isLocked());

        tokenRedisService.revokeAllTokensForUser(user.getId());
    }

    private void sendAccountCreatedByAdminMail(User user, String tempPassword) {
        String htmlContent = mailTemplateService.buildAccountCreatedByAdminEmail(
                user.getFullName(),
                user.getEmail(),
                tempPassword
        );

        SendMailRequest request = new SendMailRequest(
                user.getEmail(),
                "[TicketFlow] Your Account Has Been Created",
                htmlContent
        );

        mailService.sendHtmlMail(request);
    }

    private void sendResetPasswordByAdminMail(User user, String tempPassword) {
        String htmlContent = mailTemplateService.buildResetPasswordByAdminEmail(
                user.getFullName(),
                user.getEmail(),
                tempPassword
        );

        SendMailRequest request = new SendMailRequest(
                user.getEmail(),
                "[TicketFlow] Your Password Has Been Reset",
                htmlContent
        );

        mailService.sendHtmlMail(request);
    }
}