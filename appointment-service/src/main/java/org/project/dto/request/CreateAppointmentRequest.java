package org.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAppointmentRequest {
    @NotNull(message = "Doctor ID không được bỏ trống")
    UUID doctorId;

    @NotNull(message = "Slot ID không được bỏ trống")
    UUID slotId;

    // Patient ID - Optional (null cho guest booking)
    UUID patientId;

    @NotNull(message = "Ten bệnh nhân không được bỏ trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    String patientName;

    @NotNull(message = "Số điện thoại bệnh nhân không được bỏ trống")
    @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
    String patientPhone;

    @NotNull(message = "Email bệnh nhân không được bỏ trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    String patientEmail;

    @Size(max = 500, message = "Notes không được vượt quá 500 ký tự")
    String notes;
}
