package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "medical_profiles")
public class MedicalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Size(max = 50, message = "License number cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "License number can only contain uppercase letters and numbers")
    @Column(name = "license_number", unique = true)
    String licenseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    Specialty specialty;

    @Column(name = "qualification")
    String qualification;

    @Column(name = "years_of_experience")
    Integer yearsOfExperience;

    @Column(name = "consultation_fee", precision = 10, scale = 2)
    BigDecimal consultationFee;

    @Column(name = "bio")
    String bio;

    @Column(name = "is_doctor_approved", nullable = false)
    boolean isDoctorApproved = false;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type")
    @Column(name = "blood_type")
    String bloodType;

    @Column(name = "allergies")
    String allergies;

    @Column(name = "medical_history")
    String medicalHistory;

    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Name can only contain letters and spaces")
    @Column(name = "emergency_contact_name")
    String emergencyContactName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid emergency contact phone number")
    @Size(max = 20, message = "Emergency contact phone number cannot exceed 20 characters")
    @Column(name = "emergency_contact_phone")
    String emergencyContactPhone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "user_id", nullable = false)
    UUID userId;

}
