package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppointmentDtoResponse {
    UUID appointmentId;
    UUID doctorId;
    UUID patientId;
    UUID slotId;
    String publicCode;
    String doctorName;
    String doctorPhone;
    String patientName;
    String specialtyName;
    String patientPhone;
    String patientEmail;
    LocalDate appointmentDate;
    LocalTime startTime;
    LocalTime endTime;
    BigDecimal consultationFee;
    Status status;
    String notes;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
