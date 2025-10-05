package org.project.service;

import java.util.UUID;

public interface PaymentStatusHandler {

    void handlePaymentSuccess(UUID paymentId);

    void handlePaymentFailure(UUID paymentId);
}
