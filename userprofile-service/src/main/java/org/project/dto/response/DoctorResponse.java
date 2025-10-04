package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponse {
    UUID id;
    String fullName;
    String avatarUrl;
    String qualification;
    BigDecimal consultationFee;
    Integer yearsOfExperience;
    String gender;
    String phone;
    String specialtyName;
}
