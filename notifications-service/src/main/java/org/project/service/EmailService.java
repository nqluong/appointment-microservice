package org.project.service;

import org.project.dto.request.PasswordResetNotificationRequest;
import org.project.dto.request.PaymentNotificationRequest;

public interface EmailService {

    boolean sendPaymentSuccessEmail(PaymentNotificationRequest notificationDto);

    boolean sendPasswordResetEmail(PasswordResetNotificationRequest notificationDto);

}
