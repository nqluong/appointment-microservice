package org.project.service.impl;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.project.dto.PageResponse;
import org.project.dto.cache.DoctorAvailabilityCacheData;
import org.project.dto.cache.TimeSlot;
import org.project.dto.request.DoctorAvailabilityFilter;
import org.project.dto.response.AvailableSlotInfo;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.DoctorWithSlotsResponse;
import org.project.mapper.DoctorAvailabilityMapper2;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorAvailabilityService;
import org.project.service.RedisCacheService;
import org.project.service.UserProfileClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorAvailabilityServiceImpl implements DoctorAvailabilityService {

    private final RedisCacheService redisCacheService;
    private final UserProfileClientService userProfileClientService;
    private final DoctorAvailableSlotRepository doctorAvailableSlotRepository;

    @Value("${app.availability.batch-size:25}")
    private int batchSize;

    @Value("${app.availability.cache-ttl:3600}")
    private long cacheTtlSeconds;

    @Value("${app.availability.parallel-threshold:10}")
    private int parallelThreshold;

    @Value("${app.availability.thread-pool-size:10}")
    private int threadPoolSize;

    private ExecutorService executorService;

    private static final String PROFILE_CACHE_PREFIX = "doctor:profile:";
    private static final String AVAILABILITY_CACHE_PREFIX = "doctor:availability:";


    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        log.info("Khởi tạo ExecutorService với {} threads", threadPoolSize);
    }

    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
                log.info("ExecutorService đã được shutdown");
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }


    @Override
    public PageResponse<DoctorWithSlotsResponse> getDoctorsWithAvailableSlots(DoctorAvailabilityFilter filter) {
        log.info("Lấy danh sách bác sĩ với filter: {}", filter);
        long startTime = System.currentTimeMillis();

        try {
            Pageable pageable = createPageableFromFilter(filter);

            // Lấy danh sách doctorIds có slots
            List<UUID> doctorIds = getDoctorIdsWithAvailableSlots(filter);
            if (doctorIds.isEmpty()) {
                log.info("Không tìm thấy bác sĩ nào có slots phù hợp");
                return createEmptyPageResponse(pageable);
            }

            log.info("Tìm thấy {} bác sĩ có slots trong khoảng {} đến {}",
                    doctorIds.size(), filter.getStartDate(), filter.getEndDate());

            // Lấy thông tin chi tiết và slots
            List<DoctorWithSlotsResponse> doctorsWithSlots = fetchDoctorDetailsAndSlots(doctorIds, filter);
            if (doctorsWithSlots.isEmpty()) {
                log.info("Không có bác sĩ nào sau khi filter chi tiết");
                return createEmptyPageResponse(pageable);
            }

            // Sắp xếp theo số lượng slots available
            doctorsWithSlots = sortDoctorsByAvailability(doctorsWithSlots);

            // Phân trang
            List<DoctorWithSlotsResponse> paginatedResults = paginateResults(doctorsWithSlots, pageable);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Hoàn thành trong {}ms - Tổng: {}, Trả về: {}",
                    duration, doctorsWithSlots.size(), paginatedResults.size());

            return buildPageResponse(paginatedResults, doctorsWithSlots.size(), pageable);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách bác sĩ: {}", e.getMessage(), e);
            return createEmptyPageResponse(createPageableFromFilter(filter));
        }
    }

    @Override
    public DoctorWithSlotsResponse getDoctorAvailableSlots(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        log.info("Lấy khung giờ cho bác sĩ {} từ {} đến {}", doctorId, startDate, endDate);

        DoctorResponse doctorInfo = getDoctorInfoWithCache(doctorId);
        if (doctorInfo == null) {
            log.warn("Không tìm thấy bác sĩ {}", doctorId);
            return null;
        }

        DoctorWithSlotsResponse response = convertDoctorResponseToWithSlots(doctorInfo);

        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .useCache(true)
                .build();

        List<AvailableSlotInfo> slotInfos = getAllSlotsInRangeWithCache(doctorId, startDate, endDate, filter);
        response.setAvailableSlots(slotInfos);

        log.info("Tìm thấy {} khung giờ cho bác sĩ {}", slotInfos.size(), doctorId);
        return response;
    }

    private List<UUID> getDoctorIdsWithAvailableSlots(DoctorAvailabilityFilter filter) {
        try {
            List<UUID> doctorIds;

            // Query database theo date range
            if (filter.getStartDate() != null && filter.getEndDate() != null) {
                doctorIds = doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                        filter.getStartDate(),
                        filter.getEndDate(),
                        filter.getIsAvailable()
                );
            } else {
                doctorIds = doctorAvailableSlotRepository.findAllDistinctDoctorIds();
            }

            log.debug("Query DB: Tìm thấy {} doctorIds", doctorIds.size());

            // Filter theo specialtyIds
            if (filter.getSpecialtyIds() != null && !filter.getSpecialtyIds().isEmpty()) {
                doctorIds = filterBySpecialty(doctorIds, filter.getSpecialtyIds());
                log.debug("Sau khi filter specialty: {} doctorIds", doctorIds.size());
            }

            // Filter theo doctorIds cụ thể
            if (filter.getDoctorIds() != null && !filter.getDoctorIds().isEmpty()) {
                doctorIds.retainAll(filter.getDoctorIds());
                log.debug("Sau khi filter doctorIds: {} doctorIds", doctorIds.size());
            }

            return doctorIds;

        } catch (Exception e) {
            log.error("Lỗi query doctorIds: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<UUID> filterBySpecialty(List<UUID> doctorIds, List<UUID> specialtyIds) {
        try {
            Set<UUID> doctorsInSpecialty = new HashSet<>();

            for (UUID specialtyId : specialtyIds) {
                PageResponse<DoctorResponse> doctors = userProfileClientService
                        .getDoctorsBySpecialty(specialtyId, PageRequest.of(0, 1000));

                doctors.getContent().stream()
                        .map(DoctorResponse::getUserId)
                        .forEach(doctorsInSpecialty::add);
            }

            // Lấy intersection
            doctorIds.retainAll(doctorsInSpecialty);
            return doctorIds;

        } catch (Exception e) {
            log.error("Lỗi filter theo specialty: {}", e.getMessage());
            return doctorIds;
        }
    }

    private List<DoctorWithSlotsResponse> fetchDoctorDetailsAndSlots(
            List<UUID> doctorIds,
            DoctorAvailabilityFilter filter) {

        boolean useParallel = Boolean.TRUE.equals(filter.getUseParallelProcessing())
                && doctorIds.size() >= parallelThreshold;

        log.info("Xử lý {} doctors - Mode: {}", doctorIds.size(), useParallel ? "PARALLEL" : "SEQUENTIAL");

        return useParallel
                ? fetchDoctorsInParallel(doctorIds, filter)
                : fetchDoctorsSequentially(doctorIds, filter);
    }

    private List<DoctorWithSlotsResponse> fetchDoctorsSequentially(
            List<UUID> doctorIds,
            DoctorAvailabilityFilter filter) {

        return doctorIds.stream()
                .map(doctorId -> processSingleDoctor(doctorId, filter))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<DoctorWithSlotsResponse> fetchDoctorsInParallel(
            List<UUID> doctorIds,
            DoctorAvailabilityFilter filter) {

        List<List<UUID>> batches = partitionList(doctorIds, batchSize);
        log.debug("Chia thành {} batches, mỗi batch tối đa {} doctors", batches.size(), batchSize);

        List<CompletableFuture<List<DoctorWithSlotsResponse>>> futures = batches.stream()
                .map(batch -> CompletableFuture.supplyAsync(
                        () -> processBatch(batch, filter), executorService))
                .collect(Collectors.toList());

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);

            List<DoctorWithSlotsResponse> results = futures.stream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            log.debug("Xử lý song song hoàn thành: {} kết quả", results.size());
            return results;

        } catch (Exception e) {
            log.error("Lỗi xử lý song song: {}. Fallback sang xử lý tuần tự", e.getMessage());
            return fetchDoctorsSequentially(doctorIds, filter);
        }
    }

    private List<DoctorWithSlotsResponse> processBatch(List<UUID> doctorIds, DoctorAvailabilityFilter filter) {
        log.debug("Xử lý batch {} doctors", doctorIds.size());

        return doctorIds.stream()
                .map(doctorId -> processSingleDoctor(doctorId, filter))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private DoctorWithSlotsResponse processSingleDoctor(UUID doctorId, DoctorAvailabilityFilter filter) {
        try {
            // Lấy thông tin doctor
            DoctorResponse doctorInfo = getDoctorInfoWithCache(doctorId);
            if (doctorInfo == null) {
                log.debug("Không tìm thấy thông tin bác sĩ {}", doctorId);
                return null;
            }

            // Filter theo tên doctor
            if (!matchesDoctorNameFilter(doctorInfo, filter)) {
                return null;
            }

            // Convert sang response object
            DoctorWithSlotsResponse response = convertDoctorResponseToWithSlots(doctorInfo);

            // Lấy và filter slots
            List<AvailableSlotInfo> slots = getFilteredSlots(doctorId, filter);
            response.setAvailableSlots(slots);

            // Kiểm tra có nên include không
            if (!shouldIncludeDoctor(response, filter)) {
                return null;
            }

            return response;

        } catch (Exception e) {
            log.error("Lỗi xử lý bác sĩ {}: {}", doctorId, e.getMessage());
            return null;
        }
    }

    private DoctorResponse getDoctorInfoWithCache(UUID doctorId) {
        String cacheKey = PROFILE_CACHE_PREFIX + doctorId;

        try {
            if (redisCacheService.checkExistsKey(cacheKey)) {
                Object cached = redisCacheService.get(cacheKey);
                if (cached instanceof DoctorResponse doctorResponse) {
                    log.debug("Cache HIT - Profile bác sĩ {}", doctorId);
                    return doctorResponse;
                }
            }
        } catch (Exception e) {
            log.warn("Lỗi đọc cache profile: {}", e.getMessage());
        }

        try {
            DoctorResponse doctorResponse = userProfileClientService.getDoctorById(doctorId);
            if (doctorResponse != null) {
                redisCacheService.set(cacheKey, doctorResponse, cacheTtlSeconds, TimeUnit.SECONDS);
                return doctorResponse;
            }
        } catch (Exception e) {
            log.error("Lỗi gọi userprofile-service: {}", e.getMessage(), e);
        }

        return null;
    }


    private List<AvailableSlotInfo> getFilteredSlots(UUID doctorId, DoctorAvailabilityFilter filter) {
        if (filter.getStartDate() == null || filter.getEndDate() == null) {
            return Collections.emptyList();
        }

        return Boolean.TRUE.equals(filter.getUseCache())
                ? getAllSlotsInRangeWithCache(doctorId, filter.getStartDate(), filter.getEndDate(), filter)
                : getSlotsFromDatabaseWithFilter(doctorId, filter);
    }


    private List<AvailableSlotInfo> getAllSlotsInRangeWithCache(
            UUID doctorId,
            LocalDate startDate,
            LocalDate endDate,
            DoctorAvailabilityFilter filter) {

        List<AvailableSlotInfo> allSlots = new ArrayList<>();
        List<LocalDate> cacheMissDates = new ArrayList<>();

        List<LocalDate> dates = getDateRange(startDate, endDate);
        List<String> cacheKeys = dates.stream()
                .map(date -> AVAILABILITY_CACHE_PREFIX + doctorId + ":" + date)
                .collect(Collectors.toList());

        try {
            // Batch get từ cache
            List<Object> cachedValues = redisCacheService.mget(cacheKeys);

            for (int i = 0; i < dates.size(); i++) {
                LocalDate date = dates.get(i);
                Object cached = cachedValues.get(i);

                if (cached instanceof DoctorAvailabilityCacheData cacheData) {
                    List<AvailableSlotInfo> dailySlots = convertCacheDataToSlotInfoList(cacheData);
                    dailySlots = applySlotFilters(dailySlots, filter);
                    allSlots.addAll(dailySlots);
                } else {
                    cacheMissDates.add(date);
                }
            }

            int cacheHits = dates.size() - cacheMissDates.size();
            log.debug("Cache: HIT {}/{} ngày cho bác sĩ {}", cacheHits, dates.size(), doctorId);

        } catch (Exception e) {
            log.warn("Lỗi đọc cache: {}. Fallback sang DB", e.getMessage());
            cacheMissDates.addAll(dates);
        }

        // Xử lý cache miss
        if (!cacheMissDates.isEmpty()) {
            List<AvailableSlotInfo> dbSlots = getSlotsFromDatabase(doctorId, cacheMissDates);
            dbSlots = applySlotFilters(dbSlots, filter);
            allSlots.addAll(dbSlots);

            // Cache lại các slots vừa lấy từ DB
            cacheSlots(doctorId, dbSlots);
        }

        return allSlots;
    }

    private List<AvailableSlotInfo> getSlotsFromDatabaseWithFilter(UUID doctorId, DoctorAvailabilityFilter filter) {
        try {
            List<DoctorAvailableSlot> dbSlots = doctorAvailableSlotRepository
                    .findSlotsByDoctorAndDateRange(doctorId, filter.getStartDate(), filter.getEndDate());

            List<AvailableSlotInfo> slots = dbSlots.stream()
                    .map(this::convertDbSlotToAvailableSlotInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return applySlotFilters(slots, filter);

        } catch (Exception e) {
            log.error("Lỗi query DB: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private List<AvailableSlotInfo> getSlotsFromDatabase(UUID doctorId, List<LocalDate> dates) {
        if (dates.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDate startDate = dates.stream().min(LocalDate::compareTo).orElseThrow();
        LocalDate endDate = dates.stream().max(LocalDate::compareTo).orElseThrow();

        try {
            List<DoctorAvailableSlot> dbSlots = doctorAvailableSlotRepository
                    .findSlotsByDoctorAndDateRange(doctorId, startDate, endDate);

            return dbSlots.stream()
                    .map(this::convertDbSlotToAvailableSlotInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi query DB: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    private void cacheSlots(UUID doctorId, List<AvailableSlotInfo> slots) {
        if (slots.isEmpty()) {
            return;
        }

        try {
            // Group slots theo ngày
            Map<LocalDate, List<AvailableSlotInfo>> slotsByDate = slots.stream()
                    .collect(Collectors.groupingBy(AvailableSlotInfo::getSlotDate));

            // Cache từng ngày
            for (Map.Entry<LocalDate, List<AvailableSlotInfo>> entry : slotsByDate.entrySet()) {
                LocalDate date = entry.getKey();
                List<AvailableSlotInfo> dailySlots = entry.getValue();

                String cacheKey = AVAILABILITY_CACHE_PREFIX + doctorId + ":" + date;

                DoctorAvailabilityCacheData cacheData = DoctorAvailabilityCacheData.builder()
                        .doctorId(doctorId)
                        .date(date.toString())
                        .slots(convertAvailableSlotInfoToTimeSlots(dailySlots))
                        .build();

                redisCacheService.set(cacheKey, cacheData, cacheTtlSeconds, TimeUnit.SECONDS);
            }

            log.debug("Đã cache {} ngày cho bác sĩ {}", slotsByDate.size(), doctorId);

        } catch (Exception e) {
            log.warn("Lỗi cache slots: {}", e.getMessage());
        }
    }


    private boolean matchesDoctorNameFilter(DoctorResponse doctorInfo, DoctorAvailabilityFilter filter) {
        if (filter.getDoctorName() == null || filter.getDoctorName().trim().isEmpty()) {
            return true;
        }

        String searchName = filter.getDoctorName().toLowerCase().trim();
        return doctorInfo.getFullName() != null &&
                doctorInfo.getFullName().toLowerCase().contains(searchName);
    }

    private boolean shouldIncludeDoctor(DoctorWithSlotsResponse doctor, DoctorAvailabilityFilter filter) {
        List<AvailableSlotInfo> slots = doctor.getAvailableSlots();

        // Filter theo date range
        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            if (slots == null || slots.isEmpty()) {
                return false;
            }
        }

        // Filter hasAvailableSlots
        if (filter.getHasAvailableSlots() != null) {
            boolean hasSlots = slots != null && !slots.isEmpty();
            if (Boolean.TRUE.equals(filter.getHasAvailableSlots()) && !hasSlots) {
                return false;
            }
            if (Boolean.FALSE.equals(filter.getHasAvailableSlots()) && hasSlots) {
                return false;
            }
        }

        // Filter isAvailable
        if (filter.getIsAvailable() != null && Boolean.TRUE.equals(filter.getIsAvailable())) {
            boolean hasAvailableSlot = slots != null && slots.stream()
                    .anyMatch(slot -> Boolean.TRUE.equals(slot.getIsAvailable()));
            if (!hasAvailableSlot) {
                return false;
            }
        }

        return true;
    }

    private List<AvailableSlotInfo> applySlotFilters(List<AvailableSlotInfo> slots, DoctorAvailabilityFilter filter) {
        return slots.stream()
                .filter(slot -> {
                    // Filter startTime
                    if (filter.getStartTime() != null && slot.getStartTime().isBefore(filter.getStartTime())) {
                        return false;
                    }

                    // Filter endTime
                    if (filter.getEndTime() != null && slot.getEndTime().isAfter(filter.getEndTime())) {
                        return false;
                    }

                    // Filter isAvailable
                    if (filter.getIsAvailable() != null) {
                        return filter.getIsAvailable().equals(slot.getIsAvailable());
                    }

                    return true;
                })
                .collect(Collectors.toList());
    }

    private List<DoctorWithSlotsResponse> sortDoctorsByAvailability(List<DoctorWithSlotsResponse> doctors) {
        return doctors.stream()
                .sorted((d1, d2) -> {
                    long availableSlots1 = countAvailableSlots(d1);
                    long availableSlots2 = countAvailableSlots(d2);

                    int slotsCompare = Long.compare(availableSlots2, availableSlots1);
                    if (slotsCompare != 0) return slotsCompare;

                    String name1 = d1.getFullName() != null ? d1.getFullName() : "";
                    String name2 = d2.getFullName() != null ? d2.getFullName() : "";
                    return name1.compareTo(name2);
                })
                .collect(Collectors.toList());
    }

    private long countAvailableSlots(DoctorWithSlotsResponse doctor) {
        if (doctor.getAvailableSlots() == null) {
            return 0;
        }
        return doctor.getAvailableSlots().stream()
                .filter(slot -> Boolean.TRUE.equals(slot.getIsAvailable()))
                .count();
    }

    private List<DoctorWithSlotsResponse> paginateResults(
            List<DoctorWithSlotsResponse> allResults,
            Pageable pageable) {

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allResults.size());

        if (start >= allResults.size()) {
            return Collections.emptyList();
        }

        return allResults.subList(start, end);
    }

    private PageResponse<DoctorWithSlotsResponse> buildPageResponse(
            List<DoctorWithSlotsResponse> pageContent,
            int totalElements,
            Pageable pageable) {

        int totalPages = totalElements == 0 ? 0 :
                (int) Math.ceil((double) totalElements / pageable.getPageSize());

        return PageResponse.<DoctorWithSlotsResponse>builder()
                .content(pageContent)
                .totalElements((long) totalElements)
                .totalPages(totalPages)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .first(pageable.getPageNumber() == 0)
                .last(pageable.getPageNumber() >= totalPages - 1 || totalPages == 0)
                .empty(pageContent.isEmpty())
                .build();
    }

    private PageResponse<DoctorWithSlotsResponse> createEmptyPageResponse(Pageable pageable) {
        return PageResponse.<DoctorWithSlotsResponse>builder()
                .content(List.of())
                .totalElements(0L)
                .totalPages(0)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .first(true)
                .last(true)
                .empty(true)
                .build();
    }

    private DoctorWithSlotsResponse convertDoctorResponseToWithSlots(DoctorResponse doctorResponse) {
        return DoctorAvailabilityMapper2.toWithSlotsResponse(doctorResponse);
    }

    private AvailableSlotInfo convertDbSlotToAvailableSlotInfo(DoctorAvailableSlot dbSlot) {
        return DoctorAvailabilityMapper2.toAvailableSlotInfo(dbSlot);
    }

    private List<AvailableSlotInfo> convertCacheDataToSlotInfoList(DoctorAvailabilityCacheData cacheData) {
        return DoctorAvailabilityMapper2.toAvailableSlotInfoList(cacheData);
    }

    private AvailableSlotInfo convertTimeSlotToAvailableSlotInfo(TimeSlot timeSlot, LocalDate date) {
        return DoctorAvailabilityMapper2.toAvailableSlotInfo(timeSlot, date);
    }

    private List<TimeSlot> convertAvailableSlotInfoToTimeSlots(List<AvailableSlotInfo> slotInfos) {
        return DoctorAvailabilityMapper2.toTimeSlots(slotInfos);
    }

    private Pageable createPageableFromFilter(DoctorAvailabilityFilter filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 10;
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "createdAt";
        String sortDir = filter.getSortDirection() != null ? filter.getSortDirection() : "ASC";

        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
    }

    private List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            dates.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return dates;
    }

    private <T> List<List<T>> partitionList(List<T> list, int batchSize) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            partitions.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return partitions;
    }
}