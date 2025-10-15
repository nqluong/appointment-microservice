package org.project.service;

//import org.project.appointment_project.appoinment.model.Appointment;
//import org.project.appointment_project.payment.enums.PaymentType;

import org.project.enums.PaymentType;

import java.math.BigDecimal;

public interface PaymentAmountCalculator {
    BigDecimal calculatePaymentAmount(BigDecimal consultationFee, PaymentType paymentType);
}
