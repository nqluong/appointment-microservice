package org.project.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePaymentRequest {
    UUID appointmentId;
    PaymentType paymentType;
    PaymentMethod paymentMethod;
    BigDecimal consultationFee;
    String notes;
}

