package org.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCompleteProfileRequest {

    //User profile cho phép mọi role
    @NotBlank(message = "First name cannot be empty")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "First name can only contain letters and spaces")
    String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Last name can only contain letters and spaces")
    String lastName;

    LocalDate dateOfBirth;
    Gender gender;
    String address;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    String phone;

    String avatarUrl;

    // MedicalProfile fields chỉ cho PATIENT or DOCTOR
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type")
    String bloodType;

    @Size(max = 1000, message = "Allergies description cannot exceed 1000 characters")
    String allergies;

    @Size(max = 2000, message = "Medical history cannot exceed 2000 characters")
    String medicalHistory;

    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Name can only contain letters and spaces")
    String emergencyContactName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid emergency contact phone number")
    @Size(max = 20, message = "Emergency contact phone number cannot exceed 20 characters")
    String emergencyContactPhone;

    // Doctor-specific fields chỉ cho DOCTOR
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "License number can only contain uppercase letters and numbers")
    String licenseNumber;

    String qualification;

    @Min(value = 0, message = "Years of experience cannot be negative")
    Integer yearsOfExperience;

    @DecimalMin(value = "0.0", inclusive = false, message = "Consultation fee must be positive")
    BigDecimal consultationFee;

    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    String bio;

    String specialtyId;
}
