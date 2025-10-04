package org.project.dto.request;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorRegistrationRequest extends BaseUserRegistrationRequest {
    @NotBlank(message = "License number cannot be empty")
    @Size(max = 50, message = "License number cannot exceed 50 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "License number can only contain uppercase letters and numbers")
    String licenseNumber;

    @NotNull(message = "Specialty is required")
    UUID specialtyId;

    @NotBlank(message = "Qualification cannot be empty")
    String qualification;

    @Min(value = 0, message = "Years of experience cannot be negative")
    Integer yearsOfExperience;

    @DecimalMin(value = "0.0", message = "Consultation fee cannot be negative")
    BigDecimal consultationFee;

    String bio;
}
