//package org.project.service.impl;
//
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//
//import org.project.repository.NotificationRepository;
//import org.project.service.EmailService;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class NotificationServiceImpl implements NotificationService {
//
//    NotificationRepository notificationRepository;
//    EmailService emailService;
//    PaymentRepository paymentRepository;
//
//    @Async("notificationExecutor")
//    @Transactional
//    @Override
//    public void sendPaymentSuccessNotification(Payment payment) {
//        try {
//            Payment managedPayment = paymentRepository.findById(payment.getId())
//                    .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
//
//            Appointment appointment = managedPayment.getAppointment();
//            User patient = appointment.getPatient();
//
//            log.info("Processing payment success notification for patient: {} and payment: {}",
//                    patient.getId(), payment.getId());
//
//            // Tạo DTO chứa thông tin để gửi email
//            PaymentNotificationRequest notificationDto = buildPaymentNotificationDto(appointment, payment);
//
//            // Tạo notification record trong database
//            Notification notification = createPaymentSuccessNotification(patient, appointment, payment);
//            notification = notificationRepository.save(notification);
//
//            // Gửi email
//            boolean emailSent = emailService.sendPaymentSuccessEmail(notificationDto);
//
//            // Cập nhật status notification
//            updateNotificationStatus(notification, emailSent);
//
//            log.info("Payment success notification completed for patient: {}, Email sent: {}",
//                    patient.getId(), emailSent ? "SUCCESS" : "FAILED");
//
//        } catch (Exception e) {
//            log.error("Failed to send payment success notification for payment: {}",
//                    payment.getId(), e);
//        }
//    }
//
//    private PaymentNotificationRequest buildPaymentNotificationDto(Appointment appointment, Payment payment) {
//        User patient = appointment.getPatient();
//        User doctor = appointment.getDoctor();
//
//        String patientName = patient.getUserProfile() != null &&
//                patient.getUserProfile().getFirstName() != null ?
//                patient.getUserProfile().getFirstName() + patient.getUserProfile().getLastName() : patient.getUsername();
//
//        String doctorName = formatDoctorName(doctor);
//
//        LocalDate appointmentDate = appointment.getSlot().getSlotDate();
//        LocalTime startTime = appointment.getSlot().getStartTime();
//
//        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, startTime);
//
//        return PaymentNotificationRequest.builder()
//                .patientEmail(patient.getEmail())
//                .patientName(patientName)
//                .doctorName(doctorName)
//                .appointmentId(appointment.getId().toString())
//                .appointmentDate(appointmentDateTime)
//                .paymentAmount(payment.getAmount())
//                .transactionId(payment.getTransactionId())
//                .paymentDate(payment.getUpdatedAt())
//                .paymentType(payment.getPaymentType().toString())
//                .build();
//    }
//
//    private Notification createPaymentSuccessNotification(User patient, Appointment appointment, Payment payment) {
//        String doctorName = formatDoctorName(appointment.getDoctor());
//
//        LocalDate appointmentDate = appointment.getSlot().getSlotDate();
//        LocalTime startTime = appointment.getSlot().getStartTime();
//
//        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, startTime);
//
//        String formattedDateTime = appointmentDateTime.format(
//                DateTimeFormatter.ofPattern("dd/MM/yyyy 'lúc' HH:mm")
//        );
//
//        String title = "Thanh toán thành công - Lịch khám đã được xác nhận";
//        String message = String.format(
//                "Bạn đã thanh toán thành công %,d VNĐ cho lịch khám với bác sĩ %s vào %s. " +
//                        "Mã giao dịch: %s. Vui lòng đến đúng giờ để khám bệnh.",
//                payment.getAmount().longValue(),
//                doctorName,
//                formattedDateTime,
//                payment.getTransactionId()
//        );
//
//        return Notification.builder()
//                .user(patient)
//                .title(title)
//                .message(message)
//                .type(Type.EMAIL)
//                .status(Status.PENDING)
//                .build();
//    }
//
//    private void updateNotificationStatus(Notification notification, boolean success) {
//        notification.setStatus(success ? Status.SENT : Status.FAILED);
//        if (success) {
//            notification.setSentAt(LocalDateTime.now());
//        }
//        notificationRepository.save(notification);
//    }
//
//    private String formatDoctorName(User doctor) {
//        if (doctor.getUserProfile() != null) {
//            String firstName = doctor.getUserProfile().getFirstName();
//            String lastName = doctor.getUserProfile().getLastName();
//
//            if (firstName != null && lastName != null) {
//                String cleanedLastName = lastName.replace("BS.", "").trim();
//                return "BS. " + firstName + " " + cleanedLastName;
//            }
//        }
//        return "BS. " + doctor.getUsername();
//    }
//
//}
