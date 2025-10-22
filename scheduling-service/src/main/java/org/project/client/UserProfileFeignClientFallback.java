package org.project.client;

import java.util.UUID;

import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserProfileFeignClientFallback implements UserProfileFeignClient {

    @Override
    public DoctorResponse getDoctorById(UUID doctorId) {
        log.error("FeignClient fallback: Không thể lấy thông tin bác sĩ {}", doctorId);
        return null;
    }

    @Override
    public PageResponse<DoctorResponse> getDoctors(int page, int size, String sortBy, String sortDir) {
        log.error("FeignClient fallback: Không thể lấy danh sách bác sĩ");
        return PageResponse.<DoctorResponse>builder()
                .content(java.util.List.of())
                .totalElements(0L)
                .totalPages(0)
                .page(page)
                .size(size)
                .first(true)
                .last(true)
                .empty(true)
                .build();
    }

    @Override
    public PageResponse<DoctorResponse> getDoctorsBySpecialty(UUID specialtyId, int page, int size, String sortBy, String sortDir) {
        log.error("FeignClient fallback: Không thể lấy danh sách bác sĩ theo chuyên khoa {}", specialtyId);
        return PageResponse.<DoctorResponse>builder()
                .content(java.util.List.of())
                .totalElements(0L)
                .totalPages(0)
                .page(page)
                .size(size)
                .first(true)
                .last(true)
                .empty(true)
                .build();
    }
}
