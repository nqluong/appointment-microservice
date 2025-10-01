package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecordResponse {
    UUID id;
    UUID appointmentId;

    // Thông tin appointment liên quan
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate appointmentDate;

    @JsonFormat(pattern = "HH:mm")
    LocalTime appointmentTime;

    Status appointmentStatus;
    BigDecimal consultationFee;
    String appointmentReason;
    String appointmentNotes;

    // Thông tin doctor
    UUID doctorId;
    String doctorName;
    String doctorEmail;
    String doctorSpecialty;
    UUID doctorSpecialtyCode;
    String doctorLicenseNumber;
    String doctorQualification;
    Integer doctorYearsOfExperience;
    String doctorBio;

    // Thông tin patient
    UUID patientId;
    String patientName;
    String patientEmail;
    String patientPhone;
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate patientDateOfBirth;
    String patientGender;
    String patientBloodType;
    String patientAllergies;
    String patientMedicalHistory;
    String patientEmergencyContactName;
    String patientEmergencyContactPhone;

    // Thông tin medical record chính
    String diagnosis;
    String prescription;
    String testResults;
    String followUpNotes;
    String doctorNotes;

    // Metadata
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    String createdBy; // Doctor name who created
    String lastUpdatedBy; //
}
