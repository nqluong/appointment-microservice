package org.project.service;



import org.project.dto.request.PaymentRefundRequest;
import org.project.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RefundPolicyService {

    // Tính toán số tiền có thể hoàn
    BigDecimal calculateRefundAmount(Payment payment, PaymentRefundRequest request);

    //Tính phần trăm hoàn tiền dựa trên thời gian hủy
    BigDecimal calculateRefundPercentage(LocalDate appointmentDate, LocalDateTime cancellationDateTime);

}
