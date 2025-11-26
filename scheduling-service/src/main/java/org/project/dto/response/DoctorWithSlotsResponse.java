package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorWithSlotsResponse {

    String userId;
    String fullName;
    String phone;
    String email;
    LocalDate birthDate;
    String avatarUrl;
    String gender;

    // Medical profile information
    String specialtyName;
    String licenseNumber;
    String qualification;
    Integer yearsOfExperience;
    BigDecimal consultationFee;


     List<AvailableSlotInfo> availableSlots;
}
