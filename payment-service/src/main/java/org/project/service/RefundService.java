package org.project.service;

import org.project.events.AppointmentCancellationInitiatedEvent;

public interface RefundService {
    void processRefundForCancellation(AppointmentCancellationInitiatedEvent event);
}
