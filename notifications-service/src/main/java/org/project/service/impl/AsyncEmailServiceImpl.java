package org.project.service.impl;

import org.project.dto.request.AppointmentFailureNotificationRequest;
import org.project.dto.request.PasswordResetNotificationRequest;
import org.project.dto.request.PaymentNotificationRequest;
import org.project.service.AsyncEmailService;
import org.project.service.EmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    @Async("emailExecutor")
    @Override
    public void sendAppointmentConfirmationEmailAsync(PaymentNotificationRequest request) {
        try {
            log.info("Starting async appointment confirmation email send to: {}", request.getPatientEmail());

            boolean result = emailService.sendAppointmentConfirmationEmail(request);

            if (result) {
                log.info("Appointment confirmation email sent successfully to: {}", request.getPatientEmail());
            } else {
                log.error("Failed to send appointment confirmation email to: {}", request.getPatientEmail());
            }

        } catch (Exception e) {
            log.error("Error sending appointment confirmation email to: {}", request.getPatientEmail(), e);
        }
    }

    @Async("emailExecutor")
    @Override
    public void sendAppointmentFailureEmailAsync(AppointmentFailureNotificationRequest request) {
        try {
            log.info("Starting async appointment failure email send to: {}", request.getPatientEmail());

            boolean result = emailService.sendAppointmentFailureEmail(request);

            if (result) {
                log.info("Appointment failure email sent successfully to: {}", request.getPatientEmail());
            } else {
                log.error("Failed to send appointment failure email to: {}", request.getPatientEmail());
            }

        } catch (Exception e) {
            log.error("Error sending appointment failure email to: {}", request.getPatientEmail(), e);
        }
    }
}
