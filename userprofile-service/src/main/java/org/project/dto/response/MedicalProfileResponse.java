package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalProfileResponse {
    UUID id;
    UUID userId;
    String firstName;
    String lastName;
    String licenseNumber;
    UUID specialtyId;
    String specialtyName;
    String qualification;
    Integer yearsOfExperience;
    BigDecimal consultationFee;
    String bio;
    boolean isDoctorApproved;
}
