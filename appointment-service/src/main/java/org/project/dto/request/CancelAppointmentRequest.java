package org.project.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelAppointmentRequest {
    @Size(max = 500, message = "Lý do hủy không được vượt quá 500 ký tự")
    String reason;
}
