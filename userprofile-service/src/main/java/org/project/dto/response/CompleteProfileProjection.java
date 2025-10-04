package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteProfileProjection {
    UUID userProfileId;
    UUID userId;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Gender gender;
    String address;
    String phone;
    String avatarUrl;
    LocalDateTime userProfileUpdatedAt;

    UUID medicalProfileId;
    String bloodType;
    String allergies;
    String medicalHistory;
    String emergencyContactName;
    String emergencyContactPhone;
    String licenseNumber;
    String qualification;
    Integer yearsOfExperience;
    BigDecimal consultationFee;
    String bio;
    Boolean isDoctorApproved;
    LocalDateTime medicalProfileUpdatedAt;

    String specialtyName;
}
