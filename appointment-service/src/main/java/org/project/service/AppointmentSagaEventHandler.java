package org.project.service;

import jakarta.transaction.Transactional;
import org.project.events.*;

public interface AppointmentSagaEventHandler {
    void handlePaymentCompleted(PaymentCompletedEvent event);

    void handlePaymentFailed(PaymentFailedEvent event);

    void handleValidationFailed(ValidationFailedEvent event);

    void handlePatientValidated(PatientValidatedEvent event);

    void handleRefundProcessed(PaymentRefundProcessedEvent event);
}
