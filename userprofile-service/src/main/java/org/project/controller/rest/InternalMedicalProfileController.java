package org.project.controller.rest;

import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.project.service.MedicalProfileService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/internal/medical-profile")
@RequiredArgsConstructor
@Slf4j
public class InternalMedicalProfileController {
    private final MedicalProfileService medicalProfileService;

    /**
     * Get medical profile by user ID
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<MedicalProfileResponse> getMedicalProfileByUserId(
            @PathVariable UUID userId) {

        log.debug("Getting medical profile for user {}", userId);

        MedicalProfileResponse response = medicalProfileService.getMedicalProfileByUserId(userId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/validate-doctor/{doctorId}")
    public ResponseEntity<DoctorResponse> validateDoctor(
            @PathVariable UUID doctorId) {

        log.info("Validating doctor {} for appointment", doctorId);

        DoctorResponse response = medicalProfileService.validateDoctorForAppointment(doctorId);

        return ResponseEntity.ok(response);
    }


    @GetMapping("/doctors")
    public ResponseEntity<PageResponse<DoctorResponse>> getDoctors(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") String sortDir) {

        log.info("Internal API: Lấy danh sách bác sĩ - trang {}, kích thước {}, sắp xếp theo {} {}", 
                page, size, sortBy, sortDir);

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            PageResponse<DoctorResponse> response = medicalProfileService.getDoctors(pageable);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách bác sĩ: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/doctors/specialty/{specialtyId}")
    public ResponseEntity<PageResponse<DoctorResponse>> getDoctorsBySpecialty(
            @PathVariable UUID specialtyId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "ASC") String sortDir) {

        log.info("Internal API: Lấy danh sách bác sĩ theo chuyên khoa {} - trang {}, kích thước {}, sắp xếp theo {} {}", 
                specialtyId, page, size, sortBy, sortDir);

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            PageResponse<DoctorResponse> response = medicalProfileService.getDoctorsBySpecialty(specialtyId, pageable);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách bác sĩ theo chuyên khoa {}: {}", specialtyId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
