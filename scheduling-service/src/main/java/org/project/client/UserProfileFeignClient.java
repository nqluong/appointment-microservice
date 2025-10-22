package org.project.client;

import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "userprofile-service"
)
public interface UserProfileFeignClient {

    /**
     * Lấy thông tin bác sĩ theo ID
     */
    @GetMapping("/api/internal/medical-profile/validate-doctor/{doctorId}")
    DoctorResponse getDoctorById(@PathVariable("doctorId") UUID doctorId);

    /**
     * Lấy danh sách bác sĩ với phân trang
     */
    @GetMapping("/api/internal/medical-profile/doctors")
    PageResponse<DoctorResponse> getDoctors(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") String sortDir
    );

    /**
     * Lấy danh sách bác sĩ theo chuyên khoa với phân trang
     */
    @GetMapping("/api/internal/medical-profile/doctors/specialty/{specialtyId}")
    PageResponse<DoctorResponse> getDoctorsBySpecialty(
            @PathVariable("specialtyId") UUID specialtyId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") String sortDir
    );
}
