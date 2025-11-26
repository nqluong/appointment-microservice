package org.project.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotStatusUpdateResponse;
import org.project.exception.CustomException;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.repository.SlotStatusRepository;
import org.project.service.impl.SlotStatusServiceImpl;

@ExtendWith(MockitoExtension.class)
public class SlotStatusServiceImplTest {

    @Mock
    private SlotStatusRepository slotStatusRepository;

    @Mock
    private DoctorAvailableSlotRepository slotRepository;

    @Mock
    private SlotStatusValidationService slotStatusValidationService;

    @InjectMocks
    private SlotStatusServiceImpl slotStatusService;

    private UUID slotId;
    private DoctorAvailableSlot testSlot;

    @BeforeEach
    void setUp() {
        slotId = UUID.randomUUID();
        
        testSlot = DoctorAvailableSlot.builder()
                .id(slotId)
                .doctorId(UUID.randomUUID())
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isAvailable(false)
                .build();
    }

    @Test
    public void testReleaseSlot_Success_WhenSlotExists() {
        // Given
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
        when(slotRepository.save(any(DoctorAvailableSlot.class))).thenReturn(testSlot);

        // When
        slotStatusService.releaseSlot(slotId);

        // Then
        verify(slotRepository, times(1)).findById(slotId);
        
        ArgumentCaptor<DoctorAvailableSlot> slotCaptor = ArgumentCaptor.forClass(DoctorAvailableSlot.class);
        verify(slotRepository, times(1)).save(slotCaptor.capture());
        
        DoctorAvailableSlot savedSlot = slotCaptor.getValue();
        assertThat(savedSlot.isAvailable()).isTrue();
        assertThat(savedSlot.getId()).isEqualTo(slotId);
    }

    @Test
    public void testReleaseSlot_WhenSlotNotFound_ShouldNotThrowException() {
        // Given
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        // When
        slotStatusService.releaseSlot(slotId);

        // Then
        verify(slotRepository, times(1)).findById(slotId);
        verify(slotRepository, never()).save(any(DoctorAvailableSlot.class));
    }

    @Test
    public void testReleaseSlot_WhenSlotAlreadyAvailable_ShouldStillUpdate() {
        // Given
        testSlot.setAvailable(true);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
        when(slotRepository.save(any(DoctorAvailableSlot.class))).thenReturn(testSlot);

        // When
        slotStatusService.releaseSlot(slotId);

        // Then
        verify(slotRepository, times(1)).findById(slotId);
        
        ArgumentCaptor<DoctorAvailableSlot> slotCaptor = ArgumentCaptor.forClass(DoctorAvailableSlot.class);
        verify(slotRepository, times(1)).save(slotCaptor.capture());
        
        assertThat(slotCaptor.getValue().isAvailable()).isTrue();
    }

    @Test
    public void testReleaseSlot_ShouldChangeStatusFromFalseToTrue() {
        // Given
        testSlot.setAvailable(false);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
        
        ArgumentCaptor<DoctorAvailableSlot> slotCaptor = ArgumentCaptor.forClass(DoctorAvailableSlot.class);
        when(slotRepository.save(slotCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        slotStatusService.releaseSlot(slotId);

        // Then
        DoctorAvailableSlot capturedSlot = slotCaptor.getValue();
        assertThat(capturedSlot.isAvailable()).isTrue();
    }

    @Test
    public void testReleaseSlot_ShouldCallRepositoryMethodsInCorrectOrder() {
        // Given
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));
        when(slotRepository.save(any(DoctorAvailableSlot.class))).thenReturn(testSlot);

        // When
        slotStatusService.releaseSlot(slotId);

        // Then
        InOrder inOrder = inOrder(slotRepository);
        inOrder.verify(slotRepository).findById(slotId);
        inOrder.verify(slotRepository).save(any(DoctorAvailableSlot.class));

    }

    @Test
    public void testGetSlotDetails_Success_WhenSlotExists() {
        // Given
        UUID doctorId = UUID.randomUUID();
        testSlot.setDoctorId(doctorId);
        testSlot.setAvailable(true);
        
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));

        // When
        SlotDetailsResponse response = slotStatusService.getSlotDetails(slotId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSlotId()).isEqualTo(slotId);
        assertThat(response.getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getSlotDate()).isEqualTo(testSlot.getSlotDate());
        assertThat(response.getStartTime()).isEqualTo(testSlot.getStartTime());
        assertThat(response.getEndTime()).isEqualTo(testSlot.getEndTime());
        assertThat(response.isAvailable()).isTrue();
        
        verify(slotRepository, times(1)).findById(slotId);
    }

    @Test
    public void testGetSlotDetails_WhenSlotNotFound_ShouldThrowException() {
        // Given
        when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

        // When & Then
        try {
            slotStatusService.getSlotDetails(slotId);
            assertThat(false).as("Should throw CustomException").isTrue();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(CustomException.class);
        }
        
        verify(slotRepository, times(1)).findById(slotId);
    }

    @Test
    public void testGetSlotDetails_WhenSlotUnavailable_ShouldReturnCorrectStatus() {
        // Given: Slot tồn tại nhưng không available
        testSlot.setAvailable(false);
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));

        // When
        SlotDetailsResponse response = slotStatusService.getSlotDetails(slotId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.isAvailable()).isFalse();
        assertThat(response.getSlotId()).isEqualTo(slotId);
    }

    @Test
    public void testGetSlotDetails_ShouldReturnAllSlotProperties() {
        // Given: Slot với các thuộc tính cụ thể
        UUID doctorId = UUID.randomUUID();
        LocalDate slotDate = LocalDate.of(2025, 12, 25);
        LocalTime startTime = LocalTime.of(14, 30);
        LocalTime endTime = LocalTime.of(15, 30);
        
        testSlot.setDoctorId(doctorId);
        testSlot.setSlotDate(slotDate);
        testSlot.setStartTime(startTime);
        testSlot.setEndTime(endTime);
        testSlot.setAvailable(true);
        
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));

        // When
        SlotDetailsResponse response = slotStatusService.getSlotDetails(slotId);

        // Then
        assertThat(response.getSlotId()).isEqualTo(slotId);
        assertThat(response.getDoctorId()).isEqualTo(doctorId);
        assertThat(response.getSlotDate()).isEqualTo(slotDate);
        assertThat(response.getStartTime()).isEqualTo(startTime);
        assertThat(response.getEndTime()).isEqualTo(endTime);
        assertThat(response.isAvailable()).isTrue();
    }

    @Test
    public void testGetSlotDetails_ShouldOnlyCallFindByIdOnce() {
        // Given: Slot tồn tại
        when(slotRepository.findById(slotId)).thenReturn(Optional.of(testSlot));

        // When
        slotStatusService.getSlotDetails(slotId);

        // Then
        verify(slotRepository, times(1)).findById(slotId);
        verify(slotRepository, never()).save(any(DoctorAvailableSlot.class));
    }

    @Test
    public void testUpdateMultipleSlotStatus_Success_WithMultipleSlots() {
        // Given: Danh sách 3 requests để update slots
        UUID slotId1 = UUID.randomUUID();
        UUID slotId2 = UUID.randomUUID();
        UUID slotId3 = UUID.randomUUID();
        
        List<BatchSlotStatusRequest> requests = Arrays.asList(
            BatchSlotStatusRequest.builder()
                .slotId(slotId1)
                .isAvailable(true)
                .reason("Doctor available")
                .build(),
            BatchSlotStatusRequest.builder()
                .slotId(slotId2)
                .isAvailable(false)
                .reason("Doctor busy")
                .build(),
            BatchSlotStatusRequest.builder()
                .slotId(slotId3)
                .isAvailable(true)
                .build()
        );
        
        // Mock slots
        DoctorAvailableSlot slot1 = createSlot(slotId1, false);
        DoctorAvailableSlot slot2 = createSlot(slotId2, true);
        DoctorAvailableSlot slot3 = createSlot(slotId3, false);
        
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId1, true))
            .thenReturn(slot1);
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId2, false))
            .thenReturn(slot2);
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId3, true))
            .thenReturn(slot3);
        
        when(slotStatusRepository.save(any(DoctorAvailableSlot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<SlotStatusUpdateResponse> responses = slotStatusService.updateMultipleSlotStatus(requests);

        // Then
        assertThat(responses).hasSize(3);
        
        assertThat(responses.get(0).getSlotId()).isEqualTo(slotId1);
        assertThat(responses.get(0).getIsAvailable()).isTrue();
        assertThat(responses.get(0).getMessage()).isEqualTo("Doctor available");
        
        assertThat(responses.get(1).getSlotId()).isEqualTo(slotId2);
        assertThat(responses.get(1).getIsAvailable()).isFalse();
        assertThat(responses.get(1).getMessage()).isEqualTo("Doctor busy");
        
        assertThat(responses.get(2).getSlotId()).isEqualTo(slotId3);
        assertThat(responses.get(2).getIsAvailable()).isTrue();
        assertThat(responses.get(2).getMessage()).isEqualTo("Batch update: available");
        
        verify(slotStatusValidationService, times(1)).validateMultipleSlotStatusUpdate(requests);
        verify(slotStatusRepository, times(3)).save(any(DoctorAvailableSlot.class));
    }

    @Test
    public void testUpdateMultipleSlotStatus_WithDefaultReason_WhenReasonIsNull() {
        // Given: Request không có reason
        UUID slotId1 = UUID.randomUUID();
        UUID slotId2 = UUID.randomUUID();
        
        List<BatchSlotStatusRequest> requests = Arrays.asList(
            BatchSlotStatusRequest.builder()
                .slotId(slotId1)
                .isAvailable(true)
                .reason(null)
                .build(),
            BatchSlotStatusRequest.builder()
                .slotId(slotId2)
                .isAvailable(false)
                .reason(null)
                .build()
        );
        
        DoctorAvailableSlot slot1 = createSlot(slotId1, false);
        DoctorAvailableSlot slot2 = createSlot(slotId2, true);
        
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId1, true))
            .thenReturn(slot1);
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId2, false))
            .thenReturn(slot2);
        when(slotStatusRepository.save(any(DoctorAvailableSlot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<SlotStatusUpdateResponse> responses = slotStatusService.updateMultipleSlotStatus(requests);

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getMessage()).isEqualTo("Batch update: available");
        assertThat(responses.get(1).getMessage()).isEqualTo("Batch update: unavailable");
    }

    @Test
    public void testUpdateMultipleSlotStatus_ShouldCallValidationFirst() {
        // Given: Request list
        List<BatchSlotStatusRequest> requests = Arrays.asList(
            BatchSlotStatusRequest.builder()
                .slotId(UUID.randomUUID())
                .isAvailable(true)
                .build()
        );
        
        DoctorAvailableSlot slot = createSlot(requests.get(0).getSlotId(), false);
        when(slotStatusValidationService.findAndValidateSlotForUpdate(any(UUID.class), anyBoolean()))
            .thenReturn(slot);
        when(slotStatusRepository.save(any(DoctorAvailableSlot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        slotStatusService.updateMultipleSlotStatus(requests);

        // Then
        InOrder inOrder = inOrder(slotStatusValidationService, slotStatusValidationService);
        inOrder.verify(slotStatusValidationService).validateMultipleSlotStatusUpdate(requests);
        inOrder.verify(slotStatusValidationService).findAndValidateSlotForUpdate(any(UUID.class), anyBoolean());
    }

    @Test
    public void testUpdateMultipleSlotStatus_WithEmptyList_ShouldCallValidation() {
        // Given
        List<BatchSlotStatusRequest> requests = new ArrayList<>();

        // When & Then
        try {
            slotStatusService.updateMultipleSlotStatus(requests);
        } catch (Exception e) {
        }

        verify(slotStatusValidationService, times(1)).validateMultipleSlotStatusUpdate(requests);
    }

    @Test
    public void testUpdateMultipleSlotStatus_ShouldUpdateAllSlotsIndependently() {
        // Given: Multiple slots với trạng thái khác nhau
        UUID slotId1 = UUID.randomUUID();
        UUID slotId2 = UUID.randomUUID();
        
        List<BatchSlotStatusRequest> requests = Arrays.asList(
            BatchSlotStatusRequest.builder()
                .slotId(slotId1)
                .isAvailable(true)
                .reason("Reason 1")
                .build(),
            BatchSlotStatusRequest.builder()
                .slotId(slotId2)
                .isAvailable(false)
                .reason("Reason 2")
                .build()
        );
        
        DoctorAvailableSlot slot1 = createSlot(slotId1, false);
        DoctorAvailableSlot slot2 = createSlot(slotId2, true);
        
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId1, true))
            .thenReturn(slot1);
        when(slotStatusValidationService.findAndValidateSlotForUpdate(slotId2, false))
            .thenReturn(slot2);
        when(slotStatusRepository.save(any(DoctorAvailableSlot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When: Update multiple slots
        List<SlotStatusUpdateResponse> responses = slotStatusService.updateMultipleSlotStatus(requests);

        // Then: Mỗi slot được update độc lập
        assertThat(responses).hasSize(2);
        verify(slotStatusValidationService, times(1)).findAndValidateSlotForUpdate(slotId1, true);
        verify(slotStatusValidationService, times(1)).findAndValidateSlotForUpdate(slotId2, false);
    }

    @Test
    public void testUpdateMultipleSlotStatus_ShouldReturnResponsesInSameOrder() {
        // Given: Requests theo thứ tự cụ thể
        UUID slotId1 = UUID.randomUUID();
        UUID slotId2 = UUID.randomUUID();
        UUID slotId3 = UUID.randomUUID();
        
        List<BatchSlotStatusRequest> requests = Arrays.asList(
            BatchSlotStatusRequest.builder().slotId(slotId1).isAvailable(true).build(),
            BatchSlotStatusRequest.builder().slotId(slotId2).isAvailable(false).build(),
            BatchSlotStatusRequest.builder().slotId(slotId3).isAvailable(true).build()
        );
        
        when(slotStatusValidationService.findAndValidateSlotForUpdate(any(UUID.class), anyBoolean()))
            .thenAnswer(invocation -> {
                UUID id = invocation.getArgument(0);
                return createSlot(id, false);
            });
        when(slotStatusRepository.save(any(DoctorAvailableSlot.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<SlotStatusUpdateResponse> responses = slotStatusService.updateMultipleSlotStatus(requests);

        // Then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).getSlotId()).isEqualTo(slotId1);
        assertThat(responses.get(1).getSlotId()).isEqualTo(slotId2);
        assertThat(responses.get(2).getSlotId()).isEqualTo(slotId3);
    }

    private DoctorAvailableSlot createSlot(UUID slotId, boolean isAvailable) {
        return DoctorAvailableSlot.builder()
                .id(slotId)
                .doctorId(UUID.randomUUID())
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .isAvailable(isAvailable)
                .build();
    }
    
}
