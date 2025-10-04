package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateMedicalProfileResponse {
    UUID id;
    String bloodType;
    String allergies;
    String medicalHistory;
    String emergencyContactName;
    String emergencyContactPhone;
    LocalDateTime updatedAt;
}
