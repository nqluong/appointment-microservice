package org.project.service;

import java.time.LocalDate;
import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.request.DoctorAvailabilityFilter;
import org.project.dto.response.DoctorWithSlotsResponse;
import org.springframework.data.domain.Pageable;

public interface DoctorAvailabilityService {

    PageResponse<DoctorWithSlotsResponse> getDoctorsWithAvailableSlots(DoctorAvailabilityFilter filter);

    //Lấy danh sách các khung giờ có sẵn của một bác sĩ trong khoảng thời gian
    DoctorWithSlotsResponse getDoctorAvailableSlots(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate
    );
}
