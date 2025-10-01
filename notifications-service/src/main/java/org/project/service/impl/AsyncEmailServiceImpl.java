package org.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.PasswordResetNotificationRequest;
import org.project.service.AsyncEmailService;
import org.project.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncEmailServiceImpl implements AsyncEmailService {

    private final EmailService emailService;

    @Async("emailExecutor")
    @Override
    public void sendPasswordResetEmailAsync(
            String email,
            String userName,
            String resetToken,
            String resetUrl,
            int expiryTime) {

        try {
            log.info("Starting async password reset email send to: {}", email);

            PasswordResetNotificationRequest request = PasswordResetNotificationRequest.builder()
                    .email(email)
                    .userName(userName)
                    .resetToken(resetToken)
                    .resetUrl(resetUrl)
                    .expiryTime(expiryTime)
                    .build();

            boolean result = emailService.sendPasswordResetEmail(request);

            if (result) {
                log.info("Password reset email sent successfully to: {}", email);
            } else {
                log.error("Failed to send password reset email to: {}", email);
            }

        } catch (Exception e) {
            log.error("Error sending password reset email to: {}", email, e);
        }
    }
}
