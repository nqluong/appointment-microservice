package org.project.util;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import org.project.enums.PaymentStatus;
import org.project.model.Payment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentRefundUtil {

    public boolean isRefundable(Payment payment) {
        return payment.getPaymentStatus() == PaymentStatus.COMPLETED;
    }

    //Kiểm tra payment đã hoàn tiền chưa
    public boolean isFullyRefunded(Payment payment){
        return payment.getPaymentStatus() == PaymentStatus.REFUNDED &&
                payment.getRefundedAmount() != null &&
                payment.getRefundedAmount().compareTo(payment.getAmount()) >= 0;
    }

    public boolean isAlreadyRefunded(Payment payment) {
        return payment.getPaymentStatus() == PaymentStatus.REFUNDED ||
                (payment.getRefundedAmount() != null &&
                        payment.getRefundedAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    //Lấy số tiền có thể refund
    public BigDecimal getRefundableAmount(Payment payment) {
        BigDecimal refundedAmount = payment.getRefundedAmount() != null ?
                payment.getRefundedAmount() : BigDecimal.ZERO;
        return payment.getAmount().subtract(refundedAmount);
    }

    //Kiểm tra số tiền refund có hợp lệ không
    public boolean isValidRefundAmount(Payment payment, BigDecimal refundAmount) {
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return refundAmount.compareTo(getRefundableAmount(payment)) <= 0;
    }


     //Cập nhật thông tin refund cho payment
    public void updateRefundInfo(Payment payment, BigDecimal refundAmount,
                                 String refundTxnId, String gatewayRefundId,
                                 String reason, String gatewayResponse) {

        BigDecimal currentRefunded = payment.getRefundedAmount() != null ?
                payment.getRefundedAmount() : BigDecimal.ZERO;
        payment.setRefundedAmount(currentRefunded.add(refundAmount));

        payment.setRefundTransactionId(refundTxnId);
        payment.setGatewayRefundId(gatewayRefundId);
        payment.setRefundReason(reason);
        payment.setRefundDate(LocalDateTime.now());
        payment.setRefundGatewayResponse(gatewayResponse);
        payment.setPaymentStatus(PaymentStatus.REFUNDED);
    }

}
