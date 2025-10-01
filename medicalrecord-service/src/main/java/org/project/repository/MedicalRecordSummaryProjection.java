package org.project.repository;

import org.project.enums.Status;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public interface MedicalRecordSummaryProjection {
    UUID getId();

    // Appointment basic information
    @Value("#{target.appointment.id}")
    UUID getAppointmentId();

    @Value("#{target.appointment.appointmentDate}")
    LocalDate getAppointmentDate();

    @Value("#{target.appointment.slot != null ? target.appointment.slot.startTime : null}")
    LocalTime getAppointmentTime();

    @Value("#{target.appointment.status}")
    Status getAppointmentStatus();

    @Value("#{target.appointment.consultationFee}")
    BigDecimal getConsultationFee();

    // Doctor basic information
    @Value("#{target.appointment.doctor.id}")
    UUID getDoctorId();

    @Value("#{target.appointment.doctor.userProfile.firstName + ' ' + target.appointment.doctor.userProfile.lastName}")
    String getDoctorName();

    @Value("#{target.appointment.doctor.medicalProfile != null && target.appointment.doctor.medicalProfile.specialty != null ? target.appointment.doctor.medicalProfile.specialty.name : null}")
    String getDoctorSpecialty();

    @Value("#{target.appointment.doctor.medicalProfile != null ? target.appointment.doctor.medicalProfile.licenseNumber : null}")
    String getDoctorLicenseNumber();

    // Patient basic information
    @Value("#{target.appointment.patient.id}")
    UUID getPatientId();

    @Value("#{target.appointment.patient.userProfile.firstName + ' ' + target.appointment.patient.userProfile.lastName}")
    String getPatientName();

    @Value("#{target.appointment.patient.userProfile.gender.toString()}")
    String getPatientGender();

    @Value("#{T(java.time.Period).between(target.appointment.patient.userProfile.dateOfBirth, T(java.time.LocalDate).now()).years + ' years'}")
    String getPatientAge();

    // Medical record summaries
    @Value("#{target.diagnosis != null && target.diagnosis.length() > 100 ? target.diagnosis.substring(0, 100) + '...' : target.diagnosis}")
    String getDiagnosisSummary();

    @Value("#{target.prescription != null && target.prescription.length() > 100 ? target.prescription.substring(0, 100) + '...' : target.prescription}")
    String getPrescriptionSummary();

    @Value("#{target.testResults != null && !target.testResults.trim().isEmpty()}")
    boolean getHasTestResults();

    @Value("#{target.followUpNotes != null && !target.followUpNotes.trim().isEmpty()}")
    boolean getHasFollowUpNotes();

    // Metadata
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    @Value("#{target.appointment.doctor.userProfile.firstName + ' ' + target.appointment.doctor.userProfile.lastName}")
    String getCreatedByDoctor();


    @Value("#{T(java.time.Period).between(target.createdAt.toLocalDate(), T(java.time.LocalDate).now()).days <= 7}")
    boolean getIsRecent();

    // Note: canEdit and canView would need to be determined by the service layer
    // based on current user context and business rules
}
