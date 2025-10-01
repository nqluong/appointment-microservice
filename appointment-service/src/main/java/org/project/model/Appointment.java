package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.project.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "doctor_user_id", nullable = false)
    UUID doctorUserId;

    @Column(name = "patient_user_id", nullable = false)
    UUID patientUserId;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date", nullable = false)
    LocalDate appointmentDate;

    @Column(name = "slot_id")
    UUID slotId;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    BigDecimal consultationFee;

    @Column(name = "reason")
    String reason;

    @Column(name = "notes")
    String notes;

    @Column(name = "doctor_notes")
    String doctorNotes;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;
}
