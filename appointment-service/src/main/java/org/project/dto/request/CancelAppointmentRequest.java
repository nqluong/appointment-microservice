package org.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelAppointmentRequest {
    
    @NotBlank(message = "Lý do hủy không được để trống")
    @Size(min = 5, max = 500, message = "Lý do hủy phải có từ 5 đến 500 ký tự")
    private String reason;
}