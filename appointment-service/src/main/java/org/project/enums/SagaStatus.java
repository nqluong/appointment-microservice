package org.project.enums;

public enum SagaStatus {
    STARTED,
    SLOT_RESERVED,
    PATIENT_VALIDATED,
    DOCTOR_VALIDATED,
    COMPLETED,                  // Creation phase completed (validation done)
    PAYMENT_COMPLETED,          // Payment phase completed
    FAILED,
    COMPENSATING,
    COMPENSATED
}
