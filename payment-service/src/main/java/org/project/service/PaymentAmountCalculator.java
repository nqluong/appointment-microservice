package org.project.service;

import org.project.enums.PaymentType;

import java.math.BigDecimal;

public interface PaymentAmountCalculator {
    BigDecimal calculatePaymentAmount(BigDecimal consultationFee, PaymentType paymentType);
}
