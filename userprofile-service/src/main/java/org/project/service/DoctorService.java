package org.project.service;


import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.springframework.data.domain.Pageable;

public interface DoctorService {

    PageResponse<DoctorResponse> getAllDoctors(Pageable pageable);

    PageResponse<DoctorResponse> getDoctorsWithFilters(String specialtyName, Pageable pageable);
}
