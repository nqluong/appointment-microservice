package org.project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentRequest {
    @NotNull(message = "Appointment ID is required")
    UUID appointmentId;

    @NotNull(message = "Payment type is required")
    PaymentType paymentType;

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod;

    String notes;

}
