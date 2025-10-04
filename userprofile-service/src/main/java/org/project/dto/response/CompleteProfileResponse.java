package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompleteProfileResponse {

    UUID userProfileId;
    String firstName;
    String lastName;
    LocalDate dateOfBirth;
    Gender gender;
    String address;
    String phone;
    String avatarUrl;


    // MedicalProfile fields
    UUID medicalProfileId;
    String bloodType;
    String allergies;
    String medicalHistory;
    String emergencyContactName;
    String emergencyContactPhone;

    // Doctor-specific fields
    String licenseNumber;
    String specialtyName;
    String qualification;
    Integer yearsOfExperience;
    BigDecimal consultationFee;
    String bio;
    Boolean isDoctorApproved;

    LocalDateTime userProfileUpdatedAt;
    LocalDateTime medicalProfileUpdatedAt;

}
