package org.project.service;

import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.springframework.data.domain.Pageable;

public interface UserProfileClientService {
    
    /**
     * Lấy thông tin bác sĩ theo ID
     */
    DoctorResponse getDoctorById(UUID doctorId);
    
    /**
     * Lấy danh sách bác sĩ với phân trang
     */
    PageResponse<DoctorResponse> getDoctors(Pageable pageable);
    
    /**
     * Lấy danh sách bác sĩ theo chuyên khoa với phân trang
     */
    PageResponse<DoctorResponse> getDoctorsBySpecialty(UUID specialtyId, Pageable pageable);
}
