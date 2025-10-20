package org.project.service.impl;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.project.dto.request.AppointmentFailureNotificationRequest;
import org.project.dto.request.PasswordResetNotificationRequest;
import org.project.dto.request.PaymentNotificationRequest;
import org.project.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Hệ thống Đặt lịch Khám bệnh}")
    private String appName;

    @Override
    public boolean sendPaymentSuccessEmail(PaymentNotificationRequest notificationDto) {
        try {
            log.info("Sending payment success email to: {}", notificationDto.getPatientEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notificationDto.getPatientEmail());
            helper.setSubject("Thanh toán thành công - Lịch khám đã được xác nhận");

            String htmlContent = buildEmailContent(notificationDto);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Payment success email sent successfully to: {}", notificationDto.getPatientEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send payment success email to: {}",
                    notificationDto.getPatientEmail(), e);
            return false;
        }
    }

    @Override
    public boolean sendPasswordResetEmail(PasswordResetNotificationRequest notificationDto) {
        try {
            log.info("Sending password reset email to: {}", notificationDto.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notificationDto.getEmail());
            helper.setSubject("Yêu cầu đặt lại mật khẩu - " + appName);

            String htmlContent = buildPasswordResetEmailContent(notificationDto);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Password reset email sent successfully to: {}", notificationDto.getEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", notificationDto.getEmail(), e);
            return false;
        }
    }

    private String buildPasswordResetEmailContent(PasswordResetNotificationRequest dto) {
        Context context = new Context(new Locale("vi", "VN"));

        context.setVariable("appName", appName);
        context.setVariable("userName", dto.getUserName());
        context.setVariable("resetToken", dto.getResetToken());
        context.setVariable("resetUrl", dto.getResetUrl());
        context.setVariable("expiryTime", dto.getExpiryTime());

        return templateEngine.process("email/password-reset", context);
    }

    private String buildEmailContent(PaymentNotificationRequest dto) {
        Context context = new Context(new Locale("vi", "VN"));

        context.setVariable("appName", appName);
        context.setVariable("patientName", dto.getPatientName());
        context.setVariable("doctorName", dto.getDoctorName());
        context.setVariable("appointmentId", dto.getAppointmentId());
        context.setVariable("appointmentDate", dto.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("appointmentTime", dto.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("HH:mm")));
        context.setVariable("appointmentFullDate", dto.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy 'lúc' HH:mm",
                        new Locale("vi", "VN"))));
        context.setVariable("paymentAmount", formatCurrency(dto.getPaymentAmount()));
        context.setVariable("transactionId", dto.getTransactionId());
        context.setVariable("paymentDate", dto.getPaymentDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("paymentType", formatPaymentType(dto.getPaymentType()));

        return templateEngine.process("email/payment-success", context);
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,d VNĐ", amount.longValue());
    }

    private String formatPaymentType(String paymentType) {
        switch (paymentType.toUpperCase()) {
            case "FULL": return "Thanh toán toàn bộ";
            case "DEPOSIT": return "Thanh toán cọc";
            case "PARTIAL": return "Thanh toán một phần";
            default: return paymentType;
        }
    }

    @Override
    public boolean sendAppointmentConfirmationEmail(PaymentNotificationRequest notificationDto) {
        try {
            log.info("Sending appointment confirmation email to: {}", notificationDto.getPatientEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notificationDto.getPatientEmail());
            helper.setSubject("Đặt lịch khám thành công - Xác nhận cuộc hẹn");

            String htmlContent = buildAppointmentConfirmationContent(notificationDto);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Appointment confirmation email sent successfully to: {}", notificationDto.getPatientEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send appointment confirmation email to: {}",
                    notificationDto.getPatientEmail(), e);
            return false;
        }
    }

    @Override
    public boolean sendAppointmentFailureEmail(AppointmentFailureNotificationRequest notificationDto) {
        try {
            log.info("Sending appointment failure email to: {}", notificationDto.getPatientEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(notificationDto.getPatientEmail());
            helper.setSubject("Thông báo hủy lịch khám - Thanh toán không thành công");

            String htmlContent = buildAppointmentFailureContent(notificationDto);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Appointment failure email sent successfully to: {}", notificationDto.getPatientEmail());
            return true;

        } catch (Exception e) {
            log.error("Failed to send appointment failure email to: {}",
                    notificationDto.getPatientEmail(), e);
            return false;
        }
    }

    private String buildAppointmentConfirmationContent(PaymentNotificationRequest dto) {
        Context context = new Context(new Locale("vi", "VN"));

        context.setVariable("appName", appName);
        context.setVariable("patientName", dto.getPatientName());
        context.setVariable("doctorName", dto.getDoctorName());
        context.setVariable("appointmentId", dto.getAppointmentId());
        context.setVariable("appointmentDate", dto.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("appointmentTime", dto.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("HH:mm")));
        context.setVariable("appointmentFullDate", dto.getAppointmentDate()
                .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy 'lúc' HH:mm",
                        new Locale("vi", "VN"))));
        context.setVariable("paymentAmount", formatCurrency(dto.getPaymentAmount()));
        context.setVariable("transactionId", dto.getTransactionId());
        context.setVariable("paymentDate", dto.getPaymentDate()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("paymentType", formatPaymentType(dto.getPaymentType()));

        return templateEngine.process("email/appointment-confirmation", context);
    }

    private String buildAppointmentFailureContent(AppointmentFailureNotificationRequest dto) {
        Context context = new Context(new Locale("vi", "VN"));

        context.setVariable("appName", appName);
        context.setVariable("patientName", dto.getPatientName());
        context.setVariable("doctorName", dto.getDoctorName());
        context.setVariable("appointmentId", dto.getAppointmentId());
        
        if (dto.getAppointmentDate() != null) {
            context.setVariable("appointmentDate", dto.getAppointmentDate()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            context.setVariable("appointmentTime", dto.getAppointmentDate()
                    .format(DateTimeFormatter.ofPattern("HH:mm")));
            context.setVariable("appointmentFullDate", dto.getAppointmentDate()
                    .format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy 'lúc' HH:mm",
                            new Locale("vi", "VN"))));
        }
        
        context.setVariable("reason", dto.getReason());
        context.setVariable("transactionId", dto.getTransactionId());
        
        if (dto.getFailureTime() != null) {
            context.setVariable("failureTime", dto.getFailureTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }

        return templateEngine.process("email/appointment-failure", context);
    }


}
