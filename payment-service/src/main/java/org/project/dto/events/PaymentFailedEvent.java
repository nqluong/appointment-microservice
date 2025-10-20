package org.project.dto.events;

import java.time.LocalDateTime;
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
public class PaymentFailedEvent {
    UUID paymentId;
    UUID appointmentId;
    String transactionId;
    String reason;
    String failedService;
    LocalDateTime timestamp;

    boolean confirmedFailure;
}

