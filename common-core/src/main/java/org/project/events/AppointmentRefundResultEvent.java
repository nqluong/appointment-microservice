package org.project.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRefundResultEvent {
    private UUID appointmentId;
    private UUID paymentId;
    private UUID userId;
    private boolean refundSuccess;
    private BigDecimal refundAmount;
    private String refundType;
    private String reason;
    private LocalDateTime processedAt;
    private String errorMessage; // null if refundSuccess = true
}
