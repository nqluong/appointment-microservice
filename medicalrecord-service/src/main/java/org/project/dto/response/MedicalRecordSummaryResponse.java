package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Gender;
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
public class MedicalRecordSummaryResponse {
    UUID id;
    UUID appointmentId;

    // Thông tin appointment cơ bản
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate appointmentDate;

    @JsonFormat(pattern = "HH:mm")
    LocalTime appointmentTime;

    Status appointmentStatus;
    BigDecimal consultationFee;

    // Thông tin doctor cơ bản
    UUID doctorId;
    String doctorName;
    String doctorSpecialty;
    String doctorLicenseNumber;

    // Thông tin patient cơ bản
    UUID patientId;
    String patientName;
    Integer patientAge;
    Gender patientGender;

    String diagnosisSummary;
    String prescriptionSummary;
    boolean hasTestResults;
    boolean hasFollowUpNotes;

    // Metadata
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt;

    String createdByDoctor;
    boolean canEdit; // Based on current user permissions
    boolean canView; // Based on current user permissions
    boolean isRecent; // Created within last 7 days
}
