package org.project.service;

import org.project.dto.request.AppointmentFailureNotificationRequest;
import org.project.dto.request.PaymentNotificationRequest;

public interface AsyncEmailService {
    void sendPasswordResetEmailAsync(
            String email,
            String userName,
            String resetToken,
            String resetUrl,
            int expiryTime
    );

    void sendAppointmentConfirmationEmailAsync(PaymentNotificationRequest request);

    void sendAppointmentFailureEmailAsync(AppointmentFailureNotificationRequest request);
}
