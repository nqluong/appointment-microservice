package org.project.service;


import org.project.enums.PaymentType;

import java.util.UUID;

public interface OrderInfoBuilder {
    String buildOrderInfo(PaymentType paymentType, UUID appointmentId);
}
