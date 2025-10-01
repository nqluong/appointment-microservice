package org.project.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.RefundType;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRefundRequest {

    UUID paymentId;
    UUID appointmentId;
    String reason;

    RefundType refundType;
    BigDecimal customRefundAmount;

    public boolean isFullRefund() {
        return RefundType.FULL_REFUND.equals(refundType);
    }

    public boolean isPolicyBased() {
        return RefundType.POLICY_BASED.equals(refundType) || refundType == null;
    }

    public boolean isCustomAmount() {
        return RefundType.CUSTOM_AMOUNT.equals(refundType);
    }
}
