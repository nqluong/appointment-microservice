package org.project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.SagaStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_saga_state")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AppointmentSagaState {
    @Id
    @Column(name = "id")
    String sagaId;

    @Column(name = "appointment_id")
    UUID appointmentId;

    @Enumerated(EnumType.STRING)
    SagaStatus status; // STARTED, SLOT_RESERVED, PATIENT_VALIDATED, DOCTOR_VALIDATED, COMPLETED, FAILED

    @Column(name = "current_step")
    String currentStep;

    @Column(name = "failure_reason")
    String failureReason;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
