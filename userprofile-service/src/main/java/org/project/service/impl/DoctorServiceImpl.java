package org.project.service.impl;

import java.util.List;
import java.util.UUID;

import org.project.client.AuthServiceClient;
import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.UserIdsResponse;
import org.project.mapper.DoctorMapper;
import org.project.mapper.PageMapper;
import org.project.repository.DoctorProjection;
import org.project.repository.DoctorRepository;
import org.project.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DoctorServiceImpl implements DoctorService {

    DoctorRepository doctorRepository;
    DoctorMapper doctorMapper;
    PageMapper pageMapper;
    AuthServiceClient authServiceClient;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> getAllDoctors(Pageable pageable) {
        List<UUID> doctorUserIds = getDoctorUserIds();

        if (doctorUserIds.isEmpty()) {
            log.info("Không tìm thấy bác sĩ nào từ Auth-Service");
            return pageMapper.toPageResponse(Page.empty(pageable), doctorMapper::projectionToResponse);
        }

        Page<DoctorProjection> doctorProfilePage = doctorRepository
                .findApprovedDoctorsByUserIds(doctorUserIds, pageable);

        return pageMapper.toPageResponse(doctorProfilePage, doctorMapper::projectionToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> getDoctorsWithFilters(String specialtyName, Pageable pageable) {
        List<UUID> doctorUserIds = getDoctorUserIds();

        if (doctorUserIds.isEmpty()) {
            log.info("Không tìm thấy bác sĩ nào từ Auth-Service");
            return pageMapper.toPageResponse(Page.empty(pageable), doctorMapper::projectionToResponse);
        }

        Page<DoctorProjection> doctorPage = doctorRepository.findDoctorsWithFilters(doctorUserIds, specialtyName, pageable);

        return pageMapper.toPageResponse(doctorPage, doctorMapper::projectionToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> getDoctorsBySpecialtyId(UUID specialtyId, Pageable pageable) {
        List<UUID> doctorUserIds = getDoctorUserIds();

        if (doctorUserIds.isEmpty()) {
            log.info("Không tìm thấy bác sĩ nào từ Auth-Service");
            return pageMapper.toPageResponse(Page.empty(pageable), doctorMapper::projectionToResponse);
        }

        Page<DoctorProjection> doctorPage = doctorRepository.findDoctorsBySpecialtyId(doctorUserIds, specialtyId, pageable);

        return pageMapper.toPageResponse(doctorPage, doctorMapper::projectionToResponse);
    }

    private List<UUID> getDoctorUserIds() {
        try {
            UserIdsResponse userIdsResponse = authServiceClient.getUserIdsByRole("DOCTOR");
            if (userIdsResponse == null || userIdsResponse.getUserIds().isEmpty()) {
                throw new RuntimeException("Không tìm thấy bác sĩ nào từ Auth-Service");
            }
            return userIdsResponse.getUserIds();
        } catch (Exception ex) {
            log.error("Lỗi khi gọi Auth-Service để lấy danh sách bác sĩ", ex);
            throw new RuntimeException("Không thể kết nối tới Auth-Service để lấy danh sách bác sĩ");
        }
    }
}
