package org.project.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

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
public class DoctorProfileCacheData implements Serializable {
    UUID doctorId;
    String fullName;
    String email;
    String phoneNumber;
    String specialtyId;
    String specialtyName;
    String licenseNumber;
    Integer yearsOfExperience;
    String bio;
    BigDecimal consultationFee;
    String avatarUrl;
    Boolean isActive;
    Long cachedAt;
}
