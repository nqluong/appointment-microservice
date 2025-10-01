package org.project.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMedicalRecordRequest {
    @Size(max = 1000, message = "Chẩn đoán không được vượt quá 1000 ký tự")
    String diagnosis;

    @Size(max = 2000, message = "Đơn thuốc không được vượt quá 2000 ký tự")
    String prescription;

    @Size(max = 2000, message = "Kết quả xét nghiệm không được vượt quá 2000 ký tự")
    String testResults;

    @Size(max = 1000, message = "Ghi chú tái khám không được vượt quá 1000 ký tự")
    String followUpNotes;

    @Size(max = 1000, message = "Ghi chú của bác sĩ không được vượt quá 1000 ký tự")
    String doctorNotes;
}
