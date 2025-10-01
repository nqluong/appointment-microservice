package org.project.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;


    @Column(name = "appointment_id", nullable = false)
    UUID appointmentId;

    @Column(name = "diagnosis")
    String diagnosis;

    @Column(name = "prescription")
    String prescription;

    @Column(name = "test_results")
    String testResults;

    @Column(name = "follow_up_notes")
    String followUpNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
