package org.project.service.impl;

import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.response.AppointmentInternalResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.SlotDetailsResponse;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentType;
import org.project.enums.Status;
import org.project.events.AppointmentCancellationInitiatedEvent;
import org.project.events.AppointmentCreatedEvent;
import org.project.model.Appointment;
import org.project.utils.NameUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class AppointmentFactory {

    public Appointment createAppointment(CreateAppointmentRequest request,
                                         String publicCode,
                                         UUID effectivePatientId,
                                         DoctorResponse doctor,
                                         SlotDetailsResponse slot,
                                         boolean isGuestBooking) {
        return Appointment.builder()
                .doctorUserId(request.getDoctorId())
                .doctorName(doctor.getFullName())
                .doctorPhone(doctor.getPhone())
                .specialtyName(doctor.getSpecialtyName())
                .publicCode(publicCode)
                .patientUserId(effectivePatientId)
                .patientName(request.getPatientName())
                .patientEmail(request.getPatientEmail())
                .patientPhone(request.getPatientPhone())
                .slotId(request.getSlotId())
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .consultationFee(doctor.getConsultationFee())
                .notes(buildNotesWithBookingType(request.getNotes(), isGuestBooking))
                .status(Status.PENDING)
                .build();
    }

    public AppointmentCreatedEvent createAppointmentCreatedEvent(UUID eventId,
                                                                 UUID sagaId,
                                                                 Appointment appointment,
                                                                 SlotDetailsResponse slot) {
        return AppointmentCreatedEvent.builder()
                .eventId(eventId)
                .sagaId(sagaId)
                .appointmentId(appointment.getId())
                .patientUserId(appointment.getPatientUserId())
                .doctorUserId(appointment.getDoctorUserId())
                .slotId(appointment.getSlotId())
                .appointmentDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public AppointmentCancellationInitiatedEvent createCancellationInitiatedEvent(
            UUID eventId, UUID sagaId, Appointment appointment, String reason, String cancelledBy) {
        return AppointmentCancellationInitiatedEvent.builder()
                .eventId(eventId)
                .sagaId(sagaId)
                .appointmentId(appointment.getId())
                .patientUserId(appointment.getPatientUserId())
                .doctorUserId(appointment.getDoctorUserId())
                .slotId(appointment.getSlotId())
                .reason(reason)
                .cancelledBy(cancelledBy)
                .initiatedAt(LocalDateTime.now())
                .build();
    }

    public CreatePaymentRequest createPaymentRequest(UUID appointmentId, BigDecimal consultationFee) {
        return CreatePaymentRequest.builder()
                .appointmentId(appointmentId)
                .paymentType(PaymentType.FULL)
                .paymentMethod(PaymentMethod.VNPAY)
                .consultationFee(consultationFee)
                .notes("Thanh toán phí khám bệnh")
                .build();
    }

    public AppointmentResponse toResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .publicCode(appointment.getPublicCode())
                .appointmentId(appointment.getId())
                .doctorId(appointment.getDoctorUserId())
                .doctorName(NameUtils.formatDoctorFullName(appointment.getDoctorName()))
                .doctorPhone(appointment.getDoctorPhone())
                .specialtyName(appointment.getSpecialtyName())
                .patientId(appointment.getPatientUserId())
                .patientName(appointment.getPatientName())
                .patientEmail(appointment.getPatientEmail())
                .patientPhone(appointment.getPatientPhone())
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .consultationFee(appointment.getConsultationFee())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .doctorNotes(appointment.getDoctorNotes())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    public AppointmentInternalResponse toInternalResponse(Appointment appointment) {
        return AppointmentInternalResponse.builder()
                .appointmentId(appointment.getId())
                .doctorId(appointment.getDoctorUserId())
                .patientId(appointment.getPatientUserId())
                .appointmentDate(appointment.getAppointmentDate())
                .slotId(appointment.getSlotId())
                .consultationFee(appointment.getConsultationFee())
                .reason(appointment.getNotes())
                .notes(appointment.getNotes())
                .doctorNotes(appointment.getDoctorNotes())
                .status(appointment.getStatus())
                .createdAt(appointment.getCreatedAt())
                .updatedAt(appointment.getUpdatedAt())
                .build();
    }

    private String buildNotesWithBookingType(String notes, boolean isGuestBooking) {
        String bookingType = isGuestBooking ? "[GUEST_BOOKING]" : "[REGISTERED_USER]";
        return notes != null ? bookingType + " " + notes : bookingType;
    }
}
