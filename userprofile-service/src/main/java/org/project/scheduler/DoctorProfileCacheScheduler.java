package org.project.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.project.dto.response.DoctorResponse;
import org.project.mapper.DoctorMapper;
import org.project.repository.DoctorProjection;
import org.project.repository.DoctorRepository;
import org.project.repository.MedicalProfileRepository;
import org.project.service.AvatarUrlService;
import org.project.service.RedisCacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorProfileCacheScheduler {

    private final DoctorRepository doctorRepository;
    private final MedicalProfileRepository medicalProfileRepository;
    private final DoctorMapper doctorMapper;
    private final RedisCacheService redisCacheService;
    private final AvatarUrlService avatarUrlService;

    @Value("${cache.doctor.profile.ttl-days:7}")
    private long profileCacheTtl;

    @Value("${cache.doctor.profile.queue-key:doctor_availability_cache_queue}")
    private String availabilityQueueKey;

    @Value("${cache.doctor.profile.key-prefix:doctor:profile:}")
    private String profileCachePrefix;

    @Value("${cache.doctor.profile.page-size:50}")
    private int profilePageSize;

    @Value("${cache.doctor.profile.max-pages:10}")
    private int maxProfilePages;

    @Value("${cache.doctor.profile.update-queue:doctor:profile:update_queue}")
    private String profileUpdateQueueKey;

    @Value("${cache.doctor.profile.update-worker-delay-ms:1000}")
    private long updateWorkerDelayMs;


    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            log.info("Starting doctor profile cache initialization...");
            cacheDoctorProfiles();
        } catch (Exception ex) {
            log.error("Error initializing doctor profile cache: {}", ex.getMessage(), ex);
        }
    }


    @Async("taskExecutor")
    @Scheduled(cron = "0 0 0,6,12,18 * * *")
    public void cacheDoctorProfiles() {
        try {
            log.info("Starting doctor profile caching job...");

            int pageNumber = 0;

            while (pageNumber < maxProfilePages) {
                Pageable pageable = PageRequest.of(pageNumber, profilePageSize);
                List<UUID> doctorIds = medicalProfileRepository.findApprovedDoctorIds(pageable);

                if (doctorIds.isEmpty()) {
                    break;
                }

                log.info("Processing page {}/{}: found {} doctors", 
                    pageNumber + 1, maxProfilePages, doctorIds.size());

                Page<DoctorProjection> doctorPage = doctorRepository
                    .findApprovedDoctorsByUserIds(doctorIds, pageable);

                List<DoctorResponse> doctorResponses = doctorPage.getContent().stream()
                    .map(doctorMapper::projectionToResponse)
                    .collect(Collectors.toList());

                enrichAvatarUrlsBatch(doctorResponses);

                for (DoctorResponse doctorResponse : doctorResponses) {
                    try {
                        String cacheKey = profileCachePrefix + doctorResponse.getUserId();

                        redisCacheService.set(cacheKey, doctorResponse, profileCacheTtl, TimeUnit.DAYS);
                        redisCacheService.leftPush(availabilityQueueKey, doctorResponse.getUserId().toString());

                    } catch (Exception e) {
                        log.error("Error caching doctor profile for doctorId: {}", doctorResponse.getUserId(), e);
                    }
                }

                if (doctorIds.size() < profilePageSize) {
                    break;
                }

                pageNumber++;
            }

        } catch (Exception ex) {
            log.error("Error in doctor profile caching job: {}", ex.getMessage(), ex);
        }
    }

    private void enrichAvatarUrlsBatch(List<DoctorResponse> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return;
        }

        try {
            // Lấy danh sách tên file cần generate URL
            List<String> fileNames = doctors.stream()
                    .map(DoctorResponse::getAvatarUrl)
                    .filter(url -> url != null && !url.isEmpty())
                    .distinct()
                    .collect(Collectors.toList());

            if (fileNames.isEmpty()) {
                return;
            }

            // Gọi batch API một lần duy nhất
            Map<String, String> urlMap = avatarUrlService.generateBatchPresignedUrls(fileNames);

            // Cập nhật avatarUrl cho từng doctor
            doctors.forEach(doctor -> {
                if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                    String presignedUrl = urlMap.get(doctor.getAvatarUrl());
                    if (presignedUrl != null) {
                        doctor.setAvatarUrl(presignedUrl);
                    }
                }
            });

            log.debug("Đã enrich avatar URLs cho {} bác sĩ trong cache job", doctors.size());
        } catch (Exception e) {
            log.error("Lỗi khi enrich avatar URLs trong cache job: {}", e.getMessage(), e);
        }
    }


    @Async("taskExecutor")
    @Scheduled(fixedDelayString = "${cache.doctor.profile.update-worker-delay-ms:1000}")
    public void processCacheUpdateQueue() {
        try {
            // Lấy userId từ queue (blocking với timeout 5 giây)
            Object userIdObj = redisCacheService.rightPop(profileUpdateQueueKey, 5, TimeUnit.SECONDS);
            
            if (userIdObj == null) {
                return;
            }

            UUID userId;
            try {
                userId = UUID.fromString(userIdObj.toString());
            } catch (IllegalArgumentException e) {
                log.error("Invalid userId format in queue: {}", userIdObj, e);
                return;
            }

            log.debug("Processing cache update for userId: {}", userId);

            List<UUID> doctorIds = List.of(userId);
            Pageable pageable = PageRequest.of(0, 1);
            Page<DoctorProjection> doctorPage = doctorRepository
                    .findApprovedDoctorsByUserIds(doctorIds, pageable);

            if (doctorPage.isEmpty()) {
                log.debug("UserId {} không phải là doctor đã được approve, bỏ qua cập nhật cache", userId);
                return;
            }

            // Lấy thông tin doctor và cập nhật cache
            DoctorProjection doctorProjection = doctorPage.getContent().get(0);
            DoctorResponse doctorResponse = doctorMapper.projectionToResponse(doctorProjection);

            // Enrich avatar URL
            if (doctorResponse.getAvatarUrl() != null && !doctorResponse.getAvatarUrl().isEmpty()) {
                try {
                    String presignedUrl = avatarUrlService.generatePresignedUrl(doctorResponse.getAvatarUrl());
                    if (presignedUrl != null) {
                        doctorResponse.setAvatarUrl(presignedUrl);
                    }
                } catch (Exception e) {
                    log.error("Lỗi khi generate presigned URL cho doctor {}", userId, e);
                }
            }

            // Cập nhật cache
            String cacheKey = profileCachePrefix + userId;
            redisCacheService.set(cacheKey, doctorResponse, profileCacheTtl, TimeUnit.DAYS);

            log.info("Đã cập nhật cache cho doctor userId: {} - Last update: {}", 
                    userId, doctorResponse.getLastUpdate());

        } catch (Exception ex) {
            log.error("Error processing cache update queue: {}", ex.getMessage(), ex);
        }
    }
}

