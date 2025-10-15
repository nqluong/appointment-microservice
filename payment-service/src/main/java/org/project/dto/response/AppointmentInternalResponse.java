package org.project.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

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
public class AppointmentInternalResponse {
    UUID appointmentId;
    UUID doctorId;
    String doctorName;
    UUID patientId;
    String patientName;
    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;
    BigDecimal consultationFee;
    String status;
    LocalDateTime createdAt;
}

