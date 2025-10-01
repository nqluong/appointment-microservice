package org.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.ForgotPasswordRequest;
import org.project.dto.request.PasswordResetRequest;
import org.project.dto.response.ForgotPasswordResponse;
import org.project.dto.response.PasswordResetResponse;
import org.project.model.PasswordResetToken;
import org.project.model.User;
import org.project.repository.PasswordResetTokenRepository;
import org.project.repository.UserRepository;
import org.project.security.jwt.service.TokenService;
import org.project.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    //private final AsyncEmailService asyncEmailService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Value("${app.password-reset.token-expiry-minutes:30}")
    private int tokenExpiryMinutes;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Override
    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        try {
            User user = findActiveUserByEmail(request.getEmail());

            if (user == null) {
                log.warn("Password reset requested for non-existent/inactive email: {}", request.getEmail());
                return createSuccessResponse();
            }

            // Clean up old tokens and invalidate user sessions
            cleanupUserTokensAndSessions(user);

            // Generate and store new reset token
            String jwtToken = generateAndStoreResetToken(user);

            String resetUrl = buildResetUrl(jwtToken);
            String username = getUserDisplayName(user);
//            asyncEmailService.sendPasswordResetEmailAsync(
//                    user.getEmail(),
//                    username,
//                    jwtToken,
//                    resetUrl,
//                    tokenExpiryMinutes
//            );

            log.info("Password reset process initiated for user: {}", user.getEmail());
            return createSuccessResponse();

        } catch (Exception e) {
            log.error("Error processing forgot password request for email: {}", request.getEmail(), e);
            return createErrorResponse("Có lỗi xảy ra. Vui lòng thử lại sau.");
        }
    }

    @Override
    public PasswordResetResponse passwordReset(PasswordResetRequest request) {
        try {
            // Validate input
            PasswordResetResponse validationResult = validatePasswordResetRequest(request);
            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            // Extract user info from token
            UUID userId = tokenService.getUserIdFromPasswordResetToken(request.getToken());
            if (userId == null) {
                return createPasswordResetErrorResponse("Token không hợp lệ");
            }

            // Verify token in database
            PasswordResetToken dbToken = findValidResetToken(request.getToken());
            if (dbToken == null) {
                return createPasswordResetErrorResponse("Token không hợp lệ hoặc đã được sử dụng");
            }

            User user = dbToken.getUser();
            if (!isUserValidForPasswordReset(user)) {
                return createPasswordResetErrorResponse("Tài khoản không còn hoạt động");
            }

            // Update password and cleanup
            updateUserPassword(user, request.getNewPassword());
            markTokenAsUsedAndCleanup(dbToken, user);

            log.info("Password reset successfully completed for user: {}", user.getEmail());
            return createPasswordResetSuccessResponse();

        } catch (Exception e) {
            log.error("Error resetting password", e);
            return createPasswordResetErrorResponse("Có lỗi xảy ra khi đổi mật khẩu. Vui lòng thử lại.");
        }
    }

    @Override
    public boolean validateResetToken(String token) {
        try {
            if (!tokenService.validatePasswordResetToken(token)) {
                return false;
            }

            String hashedToken = tokenService.hashToken(token);
            PasswordResetToken dbToken = passwordResetTokenRepository
                    .findLatestByUserEmail(hashedToken)
                    .orElse(null);

            return dbToken != null && dbToken.isValid();
        } catch (Exception e) {
            log.error("Error validating reset token", e);
            return false;
        }
    }

    private User findActiveUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .filter(User::isActive)
                .orElse(null);
    }

    private void cleanupUserTokensAndSessions(User user) {
        passwordResetTokenRepository.markAllTokensAsUsedByUserId(user.getId());
        invalidateAllUserTokens(user.getId());
    }

    private String generateAndStoreResetToken(User user) {
        String jwtToken = tokenService.generatePasswordResetToken(
                user.getId(),
                user.getEmail(),
                (long) tokenExpiryMinutes
        );

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(tokenService.hashToken(jwtToken))
                .expiresAt(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .build();

        passwordResetTokenRepository.save(resetToken);
        return jwtToken;
    }

    private PasswordResetResponse validatePasswordResetRequest(PasswordResetRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return createPasswordResetErrorResponse("Mật khẩu xác nhận không khớp");
        }

        if (!tokenService.validatePasswordResetToken(request.getToken())) {
            return createPasswordResetErrorResponse("Token không hợp lệ hoặc đã hết hạn");
        }

        return PasswordResetResponse.builder().success(true).build();
    }

    private void markTokenAsUsedAndCleanup(PasswordResetToken dbToken, User user) {
        dbToken.setIsUsed(true);
        passwordResetTokenRepository.save(dbToken);

        passwordResetTokenRepository.markAllTokensAsUsedByUserId(user.getId());
        invalidateAllUserTokens(user.getId());
    }


    private PasswordResetToken findValidResetToken(String token) {
        String hashedToken = tokenService.hashToken(token);
        return passwordResetTokenRepository.findByTokenAndUseFalse(hashedToken)
                .filter(PasswordResetToken::isValid)
                .orElse(null);
    }

    private boolean isUserValidForPasswordReset(User user) {
        return user.isActive() && user.getDeletedAt() == null;
    }

    private void updateUserPassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encodedPassword);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void invalidateAllUserTokens(UUID userId) {
        try {

            log.info("Invalidating all tokens for user: {}", userId);

            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setTokensInvalidBefore(LocalDateTime.now());
                userRepository.save(user);
                log.info("User security timestamp updated for token invalidation: {}", userId);
            }

        } catch (Exception e) {
            log.error("Error invalidating user tokens for userId: {}", userId, e);
        }
    }

    private ForgotPasswordResponse createSuccessResponse() {
        return ForgotPasswordResponse.builder()
                .success(true)
                .message("Nếu email tồn tại trong hệ thống, bạn sẽ nhận được link reset mật khẩu.")
                .build();
    }

    private ForgotPasswordResponse createErrorResponse(String message) {
        return ForgotPasswordResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    private PasswordResetResponse createPasswordResetSuccessResponse() {
        return PasswordResetResponse.builder()
                .success(true)
                .message("Mật khẩu đã được cập nhật thành công")
                .build();
    }

    private PasswordResetResponse createPasswordResetErrorResponse(String message) {
        return PasswordResetResponse.builder()
                .success(false)
                .message(message)
                .build();
    }

    private String buildResetUrl(String jwtToken) {
        return resetPasswordUrl + "?token=" + jwtToken;
    }

    private String getUserDisplayName(User user) {
//        if (user.getUserProfile() != null) {
//            String firstName = user.getUserProfile().getFirstName();
//            String lastName = user.getUserProfile().getLastName();
//
//            if (firstName != null && lastName != null) {
//                return firstName + " " + lastName;
//            }
//        }
        return user.getUsername();
    }
}
