package org.project.scheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.project.dto.response.DoctorResponse;
import org.project.mapper.DoctorMapper;
import org.project.repository.DoctorProjection;
import org.project.repository.DoctorRepository;
import org.project.repository.MedicalProfileRepository;
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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DoctorProfileCacheScheduler {

    private final DoctorRepository doctorRepository;
    private final MedicalProfileRepository medicalProfileRepository;
    private final DoctorMapper doctorMapper;
    private final RedisCacheService redisCacheService;

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

                for (DoctorProjection projection : doctorPage.getContent()) {
                    try {
                        DoctorResponse doctorResponse = doctorMapper.projectionToResponse(projection);

                        String cacheKey = profileCachePrefix + projection.getUserId();

                        redisCacheService.set(cacheKey, doctorResponse, profileCacheTtl, TimeUnit.DAYS);
                        redisCacheService.leftPush(availabilityQueueKey, projection.getUserId().toString());

                    } catch (Exception e) {
                        log.error("Error caching doctor profile for doctorId: {}", projection.getUserId(), e);
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
}

