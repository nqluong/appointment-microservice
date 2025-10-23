package org.project.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import org.project.enums.Status;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentResponse {
    UUID appointmentId;
    UUID doctorId;
    String doctorName;
    String doctorPhone;
    String specialtyName;
    UUID patientId;
    String patientName;
    String patientEmail;
    String patientPhone;
    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;
    BigDecimal consultationFee;
    Status status;
    String notes;
    String doctorNotes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    
    // Payment info
    String paymentUrl;
    UUID paymentId;
}
