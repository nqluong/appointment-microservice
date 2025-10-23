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
import java.time.LocalTime;
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

    @Column(name = "doctor_name")
    String doctorName;

    @Column(name = "doctor_phone")
    String doctorPhone;

    @Column(name = "specialty_name")
    String specialtyName;

    @Column(name = "patient_user_id", nullable = false)
    UUID patientUserId;

    @Column(name = "patient_name")
    String patientName;

    @Column(name = "patient_email")
    String patientEmail;

    @Column(name = "patient_phone")
    String patientPhone;


    @Column(name = "appointment_date")
    LocalDate appointmentDate;

    @Column(name = "start_time")
    LocalTime startTime;

    @Column(name = "end_time")
    LocalTime endTime;

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
