package org.project.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.project.enums.Gender;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    String lastName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dateOfBirth;

    Gender gender;
    String address;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    String phone;

    //Medical table
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "License number can only contain uppercase letters and numbers")
    String licenseNumber;

    String specialtyId;
    String qualification;
    Integer yearsOfExperience;
    BigDecimal consultationFee;
    String bio;

    // Medical fields for patients
    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Invalid blood type")
    String bloodType;

    String allergies;
    String medicalHistory;

    @Size(max = 100, message = "Emergency contact name cannot exceed 100 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Name can only contain letters and spaces")
    String emergencyContactName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid emergency contact phone number")
    @Size(max = 20, message = "Emergency contact phone number cannot exceed 20 characters")
    String emergencyContactPhone;

    // Admin specific fields
    Boolean isDoctorApproved;
}
