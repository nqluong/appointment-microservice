package org.project.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.response.AppointmentDtoResponse;
import org.project.dto.response.AppointmentInternalResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.enums.Status;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {

    AppointmentDtoResponse getAppointmentByPublicCode(String publicCode);

    PageResponse<AppointmentDtoResponse> getUserAppointmentsByStatus(
            UUID patientId,
            List<Status> statuses,
            Pageable pageable);

    PageResponse<AppointmentDtoResponse> getDoctorAppointmentsByStatus(
            UUID doctorId,
            List<Status> statuses,
            Pageable pageable);

    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    AppointmentResponse getAppointment(UUID appointmentId);

    AppointmentDtoResponse getAppointmentDetails(UUID appointmentId);

    AppointmentResponse cancelAppointment(UUID appointmentId, String reason);

    void updateAppointmentRefundStatus(UUID appointmentId, boolean refundSuccess,
                                       BigDecimal refundAmount, String refundType);

    PageResponse<AppointmentResponse> getAppointments(UUID userId, Status status, Pageable pageable);

//    AppointmentResponse updateAppointmentStatus(UUID appointmentId, Status newStatus);
//
//    AppointmentResponse completeAppointment(UUID appointmentId);
//
//    AppointmentResponse cancelAppointment(UUID appointmentId, String reason);
//
//    AppointmentResponse startExamination(UUID appointmentId);

//    MedicalRecordResponse completeAppointmentWithMedicalRecord(CreateMedicalRecordRequest request);
//
//    MedicalRecordResponse updateMedicalRecordForAppointment(UUID appointmentId, UpdateMedicalRecordRequest request);

    List<AppointmentInternalResponse> getAffectedFullDay(UUID doctorId, LocalDate date);

    List<AppointmentInternalResponse> getAffectedByTimeRange(UUID doctorId, LocalDate date, LocalTime startTime, LocalTime endTime);

    boolean existsOverlappingAppointment(UUID patientId,LocalDate appointmentDate, LocalTime startTime, LocalTime endTime);


    long countPendingAppointmentsByPatient(UUID patientId);
}
