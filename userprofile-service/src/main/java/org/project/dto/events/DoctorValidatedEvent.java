package org.project.dto.events;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorValidatedEvent {
    String sagaId;
    UUID appointmentId;
    UUID doctorUserId;

    String doctorName;
    String doctorEmail;
    String doctorPhone;
    String specialtyName;
    BigDecimal consultationFee;

    LocalDateTime timestamp;
}
