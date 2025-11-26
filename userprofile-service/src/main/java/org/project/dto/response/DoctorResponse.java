package org.project.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorResponse {
    UUID userId;
    String fullName;
    String email;
    String gender;
    String phone;
    
    String avatarUrl;

    String qualification;
    Integer yearsOfExperience;
    BigDecimal consultationFee;
    String specialtyName;
    Boolean approved;
}
