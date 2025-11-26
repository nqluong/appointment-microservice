package org.project.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.client.AuthServiceClient;
import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.UserIdsResponse;
import org.project.mapper.DoctorMapper;
import org.project.mapper.PageMapper;
import org.project.repository.DoctorProjection;
import org.project.repository.DoctorRepository;
import org.project.service.AvatarUrlService;
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
    AvatarUrlService avatarUrlService;

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

        PageResponse<DoctorResponse> response = pageMapper.toPageResponse(doctorProfilePage, doctorMapper::projectionToResponse);
        
        // Generate presigned URLs cho avatarUrl (batch)
        enrichAvatarUrlsBatch(response.getContent());
        
        return response;
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

        PageResponse<DoctorResponse> response = pageMapper.toPageResponse(doctorPage, doctorMapper::projectionToResponse);
        
        // Generate presigned URLs cho avatarUrl (batch)
        enrichAvatarUrlsBatch(response.getContent());
        
        return response;
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

        PageResponse<DoctorResponse> response = pageMapper.toPageResponse(doctorPage, doctorMapper::projectionToResponse);
        
        enrichAvatarUrlsBatch(response.getContent());
        
        return response;
    }

    private List<UUID> getDoctorUserIds() {
        try {
            UserIdsResponse userIdsResponse = authServiceClient.getUserIdsByRole("DOCTOR");
            log.info("Lấy được {} userIds của bác sĩ từ Auth-Service", userIdsResponse.getUserIds().size());
            log.info("UserIds: {}", userIdsResponse.getUserIds());
            if (userIdsResponse == null || userIdsResponse.getUserIds().isEmpty()) {
                log.info("Không tìm thấy userIds nào với role DOCTOR từ Auth-Service");
                throw new RuntimeException("Không tìm thấy bác sĩ nào từ Auth-Service");
            }
            return userIdsResponse.getUserIds();
        } catch (Exception ex) {
            log.error("Lỗi khi gọi Auth-Service để lấy danh sách bác sĩ", ex);
            throw new RuntimeException("Không thể kết nối tới Auth-Service để lấy danh sách bác sĩ");
        }
    }
    
    private void enrichAvatarUrlsBatch(List<DoctorResponse> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return;
        }

        // Lấy danh sách tên file cần generate URL
        List<String> fileNames = doctors.stream()
                .map(DoctorResponse::getAvatarUrl)
                .filter(url -> url != null && !url.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (fileNames.isEmpty()) {
            return;
        }

        Map<String, String> urlMap = avatarUrlService.generateBatchPresignedUrls(fileNames);

        doctors.forEach(doctor -> {
            if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                String presignedUrl = urlMap.get(doctor.getAvatarUrl());
                if (presignedUrl != null) {
                    doctor.setAvatarUrl(presignedUrl);
                }
            }
        });

        log.debug("Đã enrich avatar URLs cho {} bác sĩ", doctors.size());
    }
}
