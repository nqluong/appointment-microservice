package org.project.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientRegistrationRequest extends BaseUserRegistrationRequest{
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
}
