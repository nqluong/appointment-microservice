package org.project.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.PasswordResetNotificationRequest;
import org.project.dto.request.PaymentNotificationRequest;
import org.project.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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


}
