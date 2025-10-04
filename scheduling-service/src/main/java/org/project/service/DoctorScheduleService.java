package org.project.service;


import org.project.dto.PageResponse;
import org.project.dto.request.DoctorScheduleCreateRequest;
import org.project.dto.request.DoctorScheduleUpdateRequest;
import org.project.dto.request.DoctorSearchRequest;
import org.project.dto.response.DoctorScheduleResponse;
import org.project.dto.response.DoctorSearchResponse;

import java.util.UUID;

public interface DoctorScheduleService {

//    DoctorScheduleResponse createDoctorSchedule(DoctorScheduleCreateRequest request);
//
//    DoctorScheduleResponse getDoctorSchedule(UUID doctorId);
//
//    DoctorScheduleResponse updateDoctorSchedule(UUID doctorId, DoctorScheduleUpdateRequest request);

    void deleteDoctorSchedule(UUID doctorId);

   // PageResponse<DoctorSearchResponse> searchDoctors(DoctorSearchRequest request);
}
