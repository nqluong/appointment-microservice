package org.project.service;


import org.project.dto.PageResponse;
import org.project.dto.request.CreateAbsenceRequest;
import org.project.dto.request.UpdateAbsenceRequest;
import org.project.dto.response.DoctorAbsenceResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DoctorAbsenceService {

    DoctorAbsenceResponse createAbsence(CreateAbsenceRequest request);

    DoctorAbsenceResponse updateAbsence(UUID absenceId, UpdateAbsenceRequest request);

    DoctorAbsenceResponse getAbsenceById(UUID absenceId);

    PageResponse<DoctorAbsenceResponse> getAbsencesByDoctor(UUID doctorUserId, Pageable pageable);

    List<DoctorAbsenceResponse> getAbsencesInDateRange(UUID doctorUserId, LocalDate startDate, LocalDate endDate);

    List<DoctorAbsenceResponse> getFutureAbsences(UUID doctorUserId);

    void deleteAbsence(UUID absenceId);

    boolean isDoctorAbsentOnDate(UUID doctorUserId, LocalDate date);

    int cleanupPastAbsences(LocalDate cutoffDate);

}
