package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkSlotGenerationResponse {
    LocalDate startDate;
    LocalDate endDate;
    int totalDoctors;
    int successfulGenerations;
    int failedGenerations;
    int totalSlotsGenerated;
    List<DoctorSlotResult> results;
    String message;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DoctorSlotResult {
        java.util.UUID doctorId;
        boolean success;
        int slotsGenerated;
        String errorMessage;
    }
}
