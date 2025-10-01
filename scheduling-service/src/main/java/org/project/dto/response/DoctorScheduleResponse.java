package org.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorScheduleResponse {
    UUID id;
    UUID doctorId;
    String doctorName;
    List<ScheduleEntryResponse> scheduleEntries;
    String notes;
    String timezone;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
