package org.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SpecialtyRequest {
    @NotBlank(message = "Specialty name cannot be empty")
    @Size(min = 2, max = 100, message = "Specialty name must be between 2 and 100 characters")
    String name;

    String description;

    Boolean isActive;
}
