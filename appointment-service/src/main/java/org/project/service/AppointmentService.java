package org.project.service;

import org.project.dto.PageResponse;
import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.response.AppointmentDtoResponse;
import org.project.dto.response.AppointmentResponse;
import org.project.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {
    PageResponse<AppointmentDtoResponse> getUserAppointmentsByStatus(
            UUID patientId,
            List<Status> statuses,
            Pageable pageable);

    PageResponse<AppointmentDtoResponse> getDoctorAppointmentsByStatus(
            UUID doctorId,
            List<Status> statuses,
            Pageable pageable);

    // AppointmentResponse createAppointment(CreateAppointmentRequest request);

    //PageResponse<AppointmentResponse> getAppointments(UUID userId, Status status, Pageable pageable);

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
}
