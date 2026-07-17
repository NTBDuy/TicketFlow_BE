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
import com.duyntb.ticketflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

    public PageResponse<UserResponse> getUsers(String keyword, int page, int size) {
        Specification<User> spec = Specification.where(hasUser(keyword));

        Pageable pageable = PageRequest.of(page, size);

        Page<UserResponse> userPage = userRepository.findAll(spec, pageable)
                .map(UserResponse::from);

        return PageResponse.from(userPage);
    }

    public UserResponse getUserById(Long id) {
        return UserResponse.from(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    public CreateUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already exists");
        }

        String tempPwd = PasswordGenerator.generate(12);
        String pwd = passwordEncoder.encode(tempPwd);

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(pwd)
                .role(Role.USER)
                .mustChangePassword(true)
                .build();

        User savedUser = userRepository.save(user);
        sendAccountCreatedByAdminMail(savedUser, tempPwd);
        return CreateUserResponse.from(savedUser, tempPwd);
    }

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

    @Transactional
    public void updateUserRole(Long id, UpdateUserRoleRequest request) {
        Long currentUserId = securityUtils.getCurrentUserId();
        if (currentUserId.equals(id)) {
            throw new BadRequestException("You cannot change your own role");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() == request.role()) {
            return;
        }

        user.setRole(request.role());

        tokenRedisService.revokeAllTokensForUser(id);
    }

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

    public void resetPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String tempPassword = PasswordGenerator.generate(12);
        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setMustChangePassword(true);
        userRepository.save(user);

        sendResetPasswordByAdminMail(user, tempPassword);
    }

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