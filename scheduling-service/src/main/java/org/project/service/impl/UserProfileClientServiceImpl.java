package org.project.service.impl;

import java.util.UUID;

import org.project.client.UserProfileFeignClient;
import org.project.client.UserProfileServiceClient;
import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.project.service.UserProfileClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileClientServiceImpl implements UserProfileClientService {

    private final UserProfileServiceClient userProfileServiceClient;
    private final UserProfileFeignClient userProfileFeignClient;
    
    @Value("${app.user-profile.use-feign:true}")
    boolean useFeignClient;

    @Override
    public DoctorResponse getDoctorById(UUID doctorId) {
        try {
            if (useFeignClient) {
                DoctorResponse response = userProfileFeignClient.getDoctorById(doctorId);
                return response;
            } else {
                DoctorResponse response = userProfileServiceClient.getDoctorById(doctorId);
                return response;
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin bác sĩ {}: {}", doctorId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public PageResponse<DoctorResponse> getDoctors(Pageable pageable) {
        try {
            String sortBy = pageable.getSort().isSorted() ? 
                    pageable.getSort().iterator().next().getProperty() : "createdAt";
            String sortDir = pageable.getSort().isSorted() ? 
                    pageable.getSort().iterator().next().getDirection().name() : "ASC";
            
            if (useFeignClient) {
                PageResponse<DoctorResponse> response = userProfileFeignClient.getDoctors(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sortBy,
                        sortDir
                );
                return response;
            } else {
                PageResponse<DoctorResponse> response = userProfileServiceClient.getDoctors(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sortBy,
                        sortDir
                );
                return response;
            }
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách bác sĩ: {}", e.getMessage());
            return PageResponse.<DoctorResponse>builder()
                    .content(java.util.List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }
    }

    @Override
    public PageResponse<DoctorResponse> getDoctorsBySpecialty(UUID specialtyId, Pageable pageable) {
        try {
            String sortBy = pageable.getSort().isSorted() ? 
                    pageable.getSort().iterator().next().getProperty() : "createdAt";
            String sortDir = pageable.getSort().isSorted() ? 
                    pageable.getSort().iterator().next().getDirection().name() : "ASC";
            
            if (useFeignClient) {
                return userProfileFeignClient.getDoctorsBySpecialty(
                        specialtyId,
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sortBy,
                        sortDir
                );
            } else {
                return userProfileServiceClient.getDoctorsBySpecialty(
                        specialtyId,
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        sortBy,
                        sortDir
                );
            }
        } catch (Exception e) {
            return PageResponse.<DoctorResponse>builder()
                    .content(java.util.List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .page(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }
    }
}
