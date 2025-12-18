package org.project.dto.response;

import java.time.LocalDateTime;
import java.util.List;
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
public class SpecialtyResponse {
    UUID specialtyId;
    String name;
    String description;
    Boolean isActive;
    List<DoctorResponse> doctors;
    Integer doctorCount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
