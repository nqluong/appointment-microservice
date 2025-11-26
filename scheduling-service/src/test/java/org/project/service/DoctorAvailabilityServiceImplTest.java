package org.project.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.dto.PageResponse;
import org.project.dto.request.DoctorAvailabilityFilter;
import org.project.dto.response.DoctorResponse;
import org.project.dto.response.DoctorWithSlotsResponse;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.impl.DoctorAvailabilityServiceImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceImplTest {

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private UserProfileClientService userProfileClientService;

    @Mock
    private DoctorAvailableSlotRepository doctorAvailableSlotRepository;

    @InjectMocks
    private DoctorAvailabilityServiceImpl doctorAvailabilityService;

    private UUID doctorId1;
    private UUID doctorId2;
    private UUID doctorId3;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        doctorId1 = UUID.randomUUID();
        doctorId2 = UUID.randomUUID();
        doctorId3 = UUID.randomUUID();
        startDate = LocalDate.now();
        endDate = startDate.plusDays(7);

        ReflectionTestUtils.setField(doctorAvailabilityService, "batchSize", 25);
        ReflectionTestUtils.setField(doctorAvailabilityService, "cacheTtlSeconds", 3600L);
        ReflectionTestUtils.setField(doctorAvailabilityService, "parallelThreshold", 10);
        ReflectionTestUtils.setField(doctorAvailabilityService, "threadPoolSize", 10);
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithValidFilter_ShouldReturnPagedResults() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> doctorIds = Arrays.asList(doctorId1, doctorId2);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(doctorIds);

        // Mock doctor info
        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        DoctorResponse doctor2 = createDoctorResponse(doctorId2, "Dr. Jane Smith", "Neurology");
        
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);
        when(userProfileClientService.getDoctorById(doctorId2)).thenReturn(doctor2);

        // Mock slots
        List<DoctorAvailableSlot> slots1 = createAvailableSlots(doctorId1, 3);
        List<DoctorAvailableSlot> slots2 = createAvailableSlots(doctorId2, 2);

        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots1);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId2), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots2);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isEmpty()).isFalse();

        verify(doctorAvailableSlotRepository).findDistinctDoctorIdsByDateRange(startDate, endDate, true);
        verify(userProfileClientService, times(2)).getDoctorById(any(UUID.class));
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithNoMatchingDoctors_ShouldReturnEmptyPage() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .isAvailable(true)
                .page(0)
                .size(10)
                .build();

        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(Collections.emptyList());

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        assertThat(result.isEmpty()).isTrue();

        verify(doctorAvailableSlotRepository).findDistinctDoctorIdsByDateRange(startDate, endDate, true);
        verify(userProfileClientService, never()).getDoctorById(any(UUID.class));
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithSpecialtyFilter_ShouldFilterBySpecialty() {
        // Given
        UUID specialtyId = UUID.randomUUID();
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .specialtyIds(Collections.singletonList(specialtyId))
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> allDoctorIds = Arrays.asList(doctorId1, doctorId2, doctorId3);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(allDoctorIds);

        // Mock specialty filtering
        PageResponse<DoctorResponse> specialtyDoctors = PageResponse.<DoctorResponse>builder()
                .content(Arrays.asList(
                        createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology"),
                        createDoctorResponse(doctorId2, "Dr. Jane Smith", "Cardiology")))
                .build();

        when(userProfileClientService.getDoctorsBySpecialty(eq(specialtyId), any(PageRequest.class)))
                .thenReturn(specialtyDoctors);

        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        DoctorResponse doctor2 = createDoctorResponse(doctorId2, "Dr. Jane Smith", "Cardiology");
        
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);
        when(userProfileClientService.getDoctorById(doctorId2)).thenReturn(doctor2);

        List<DoctorAvailableSlot> slots = createAvailableSlots(doctorId1, 2);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        verify(userProfileClientService).getDoctorsBySpecialty(eq(specialtyId), any(PageRequest.class));
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithDoctorNameFilter_ShouldFilterByName() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .doctorName("John")
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> doctorIds = Arrays.asList(doctorId1, doctorId2);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(doctorIds);

        // Only doctor1 matches the name filter
        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        DoctorResponse doctor2 = createDoctorResponse(doctorId2, "Dr. Jane Smith", "Neurology");
        
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);
        when(userProfileClientService.getDoctorById(doctorId2)).thenReturn(doctor2);

        List<DoctorAvailableSlot> slots = createAvailableSlots(doctorId1, 2);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getFullName()).contains("John");
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithPagination_ShouldReturnCorrectPage() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .isAvailable(true)
                .page(1)  // Second page
                .size(1)  // 1 item per page
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> doctorIds = Arrays.asList(doctorId1, doctorId2, doctorId3);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(doctorIds);

        // Mock doctor responses
        when(userProfileClientService.getDoctorById(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID id = invocation.getArgument(0);
                    return createDoctorResponse(id, "Dr. Test " + id, "Specialty");
                });

        List<DoctorAvailableSlot> slots = createAvailableSlots(doctorId1, 2);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                any(UUID.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(1);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(3);
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithTimeRangeFilter_ShouldFilterByTime() {
        // Given
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(12, 0);
        
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .startTime(startTime)
                .endTime(endTime)
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> doctorIds = Collections.singletonList(doctorId1);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(doctorIds);

        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);

        // Create slots with different times
        List<DoctorAvailableSlot> slots = createAvailableSlotsWithTimes(
                doctorId1, 
                Arrays.asList(
                        LocalTime.of(8, 0),  // Before filter
                        LocalTime.of(10, 0), // Within filter
                        LocalTime.of(13, 0)  // After filter
                )
        );
        
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        
        DoctorWithSlotsResponse doctor = result.getContent().get(0);
        assertThat(doctor.getAvailableSlots()).isNotEmpty();
        
        // Verify all slots are within time range
        doctor.getAvailableSlots().forEach(slot -> {
            assertThat(slot.getStartTime()).isAfterOrEqualTo(startTime);
            assertThat(slot.getEndTime()).isBeforeOrEqualTo(endTime);
        });
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WhenExceptionOccurs_ShouldReturnEmptyPage() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .page(0)
                .size(10)
                .build();

        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                any(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithDoctorIdsFilter_ShouldOnlyReturnSpecifiedDoctors() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .doctorIds(Collections.singletonList(doctorId1))
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> allDoctorIds = Arrays.asList(doctorId1, doctorId2, doctorId3);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(allDoctorIds);

        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);

        List<DoctorAvailableSlot> slots = createAvailableSlots(doctorId1, 2);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(doctorId1.toString());
        verify(userProfileClientService, times(1)).getDoctorById(doctorId1);
    }

    @Test
    void testGetDoctorsWithAvailableSlots_ShouldSortByAvailableSlots() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> doctorIds = Arrays.asList(doctorId1, doctorId2);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(doctorIds);

        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        DoctorResponse doctor2 = createDoctorResponse(doctorId2, "Dr. Jane Smith", "Neurology");
        
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);
        when(userProfileClientService.getDoctorById(doctorId2)).thenReturn(doctor2);

        // doctor1 has 5 slots, doctor2 has 2 slots
        List<DoctorAvailableSlot> slots1 = createAvailableSlots(doctorId1, 5);
        List<DoctorAvailableSlot> slots2 = createAvailableSlots(doctorId2, 2);

        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots1);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId2), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots2);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        
        // Should be sorted by available slots count (descending)
        assertThat(result.getContent().get(0).getAvailableSlots().size())
                .isGreaterThan(result.getContent().get(1).getAvailableSlots().size());
    }

    @Test
    void testGetDoctorsWithAvailableSlots_WithHasAvailableSlotsFilter_ShouldFilterCorrectly() {
        // Given
        DoctorAvailabilityFilter filter = DoctorAvailabilityFilter.builder()
                .startDate(startDate)
                .endDate(endDate)
                .hasAvailableSlots(true)
                .isAvailable(true)
                .page(0)
                .size(10)
                .useCache(false)
                .useParallelProcessing(false)
                .build();

        List<UUID> doctorIds = Arrays.asList(doctorId1, doctorId2);
        
        when(doctorAvailableSlotRepository.findDistinctDoctorIdsByDateRange(
                startDate, endDate, true))
                .thenReturn(doctorIds);

        DoctorResponse doctor1 = createDoctorResponse(doctorId1, "Dr. John Doe", "Cardiology");
        DoctorResponse doctor2 = createDoctorResponse(doctorId2, "Dr. Jane Smith", "Neurology");
        
        when(userProfileClientService.getDoctorById(doctorId1)).thenReturn(doctor1);
        when(userProfileClientService.getDoctorById(doctorId2)).thenReturn(doctor2);

        // doctor1 has slots, doctor2 has no slots
        List<DoctorAvailableSlot> slots1 = createAvailableSlots(doctorId1, 3);
        List<DoctorAvailableSlot> slots2 = Collections.emptyList();

        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId1), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots1);
        when(doctorAvailableSlotRepository.findSlotsByDoctorAndDateRange(
                eq(doctorId2), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(slots2);

        // When
        PageResponse<DoctorWithSlotsResponse> result = 
                doctorAvailabilityService.getDoctorsWithAvailableSlots(filter);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUserId()).isEqualTo(doctorId1.toString());
        assertThat(result.getContent().get(0).getAvailableSlots()).isNotEmpty();
    }

    // Helper methods
    private DoctorResponse createDoctorResponse(UUID doctorId, String fullName, String specialty) {
        return DoctorResponse.builder()
                .userId(doctorId)
                .fullName(fullName)
                .specialtyName(specialty)
                .qualification("MBBS, MD")
                .yearsOfExperience(10)
                .consultationFee(new BigDecimal("150.00"))
                .phone("1234567890")
                .avatarUrl("https://example.com/avatar.jpg")
                .build();
    }

    private List<DoctorAvailableSlot> createAvailableSlots(UUID doctorId, int count) {
        List<DoctorAvailableSlot> slots = new ArrayList<>();
        LocalDate date = startDate;
        
        for (int i = 0; i < count; i++) {
            DoctorAvailableSlot slot = new DoctorAvailableSlot();
            slot.setId(UUID.randomUUID());
            slot.setDoctorId(doctorId);
            slot.setSlotDate(date);
            slot.setStartTime(LocalTime.of(9 + i, 0));
            slot.setEndTime(LocalTime.of(10 + i, 0));
            slot.setAvailable(true);
            slots.add(slot);
        }
        
        return slots;
    }

    private List<DoctorAvailableSlot> createAvailableSlotsWithTimes(UUID doctorId, List<LocalTime> startTimes) {
        List<DoctorAvailableSlot> slots = new ArrayList<>();
        
        for (LocalTime startTime : startTimes) {
            DoctorAvailableSlot slot = new DoctorAvailableSlot();
            slot.setId(UUID.randomUUID());
            slot.setDoctorId(doctorId);
            slot.setSlotDate(startDate);
            slot.setStartTime(startTime);
            slot.setEndTime(startTime.plusHours(1));
            slot.setAvailable(true);
            slots.add(slot);
        }
        
        return slots;
    }
}