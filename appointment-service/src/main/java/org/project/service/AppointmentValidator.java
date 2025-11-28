package org.project.service;

import org.project.dto.request.CreateAppointmentRequest;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface AppointmentValidator {
    void validatePatientInfo(CreateAppointmentRequest request);

    void validatePatient(UUID patientId);

    DoctorResponse validateDoctor(UUID doctorId);

    SlotDetailsResponse validateAndReserveSlot(UUID slotId, UUID doctorId, UUID patientId);

    void checkOverlappingAppointment(UUID patientId, LocalDate date,
                                     LocalTime startTime, LocalTime endTime);

    void validateCancellation(Appointment appointment);
}
