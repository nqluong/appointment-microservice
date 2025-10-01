package org.project.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorSearchRequest {
    String doctorName;

    UUID specialtyId;
    String specialtyName;

    Integer minExperience;
    Integer maxExperience;

    LocalDate availableDate;
    LocalTime preferredStartTime;
    LocalTime preferredEndTime;

    BigDecimal minConsultationFee;
    BigDecimal maxConsultationFee;

    Boolean isApproved;

    String qualification;

    int page = 0;
    int size = 20;
    String sortBy = "createdAt";
    String sortDirection = "desc";
}
