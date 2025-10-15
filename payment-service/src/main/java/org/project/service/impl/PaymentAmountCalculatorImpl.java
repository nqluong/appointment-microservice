package org.project.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.project.enums.PaymentType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.PaymentAmountCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Slf4j
public class PaymentAmountCalculatorImpl implements PaymentAmountCalculator {

    private static final BigDecimal DEPOSIT_RATE = BigDecimal.valueOf(0.3);
    private static final BigDecimal REMAINING_RATE = BigDecimal.valueOf(0.7);

    @Override
    public BigDecimal calculatePaymentAmount(BigDecimal consultationFee, PaymentType paymentType) {

        switch (paymentType) {
            case DEPOSIT:
                return consultationFee.multiply(DEPOSIT_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
            case FULL:
                return consultationFee;
            case REMAINING:
                return consultationFee.multiply(REMAINING_RATE)
                        .setScale(2, RoundingMode.HALF_UP);
            default:
                throw new CustomException(ErrorCode.INVALID_PAYMENT_TYPE);
        }
    }
}
