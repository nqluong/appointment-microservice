package org.project.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.dto.response.SlotStatusUpdateResponse;
import org.project.enums.ValidationType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.repository.SlotStatusRepository;
import org.project.service.SlotStatusService;
import org.project.service.SlotStatusValidationService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotStatusServiceImpl implements SlotStatusService {

    SlotStatusRepository slotStatusRepository;
    DoctorAvailableSlotRepository slotRepository;
    SlotStatusValidationService slotStatusValidationService;

    @Override
    public SlotStatusUpdateResponse markSlotAvailable(UUID slotId) {
        return updateSlotStatus(slotId, true, "Slot marked as available");
    }

    @Override
    public SlotStatusUpdateResponse markSlotUnavailable(UUID slotId) {
        return updateSlotStatus(slotId, false, "Slot marked as unavailable");
    }


    @Override
    @Transactional
    public List<SlotStatusUpdateResponse> updateMultipleSlotStatus(List<BatchSlotStatusRequest> requests) {
        slotStatusValidationService.validateMultipleSlotStatusUpdate(requests);

        return requests.stream()
                .map(request -> updateSlotStatus(
                        request.getSlotId(),
                        request.getIsAvailable(),
                        request.getReason() != null ? request.getReason() :
                                (request.getIsAvailable() ? "Batch update: available" : "Batch update: unavailable")
                ))
                .collect(Collectors.toList());
    }

    /**
     * Chỉ dùng optimistic lock để handle race condition
     */
    @Override
    @Transactional
    public SlotReservationResponse reserveSlot(SlotReservationRequest request) {
        log.info("Reserving slot {} for patient {}", request.getSlotId(), request.getPatientId());

        try {
            // Get slot với optimistic lock
            DoctorAvailableSlot slot = slotRepository.findByIdWithLock(request.getSlotId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));

            // Validate slot
            validateSlotForReservation(slot, request);

            if (!slot.isAvailable()) {
                return SlotReservationResponse.builder()
                        .success(false)
                        .slotId(slot.getId())
                        .message("Slot đã được đặt bởi người khác, vui lòng chọn slot khác")
                        .build();
            }

            // Reserve slot
            slot.setAvailable(false);
            slotRepository.save(slot); // Optimistic lock sẽ throw exception nếu conflict

            log.info("Slot {} reserved successfully for patient {}", slot.getId(), request.getPatientId());

            return SlotReservationResponse.builder()
                    .success(true)
                    .slotId(slot.getId())
                    .message("Đặt chỗ slot thành công")
                    .build();

        } catch (ObjectOptimisticLockingFailureException e) {
            // Race condition - người khác vừa đặt
            log.warn("Optimistic lock failure for slot {} - concurrent booking detected", request.getSlotId());

            return SlotReservationResponse.builder()
                    .success(false)
                    .slotId(request.getSlotId())
                    .message("Slot vừa được đặt bởi người khác, vui lòng chọn slot khác")
                    .build();
        }
    }

    @Override
    @Transactional
    public void releaseSlot(UUID slotId) {
        log.info("Releasing slot {}", slotId);

        slotRepository.findById(slotId).ifPresentOrElse(
                slot -> {
                    slot.setAvailable(true);
                    slotRepository.save(slot);
                    log.info("Slot {} released successfully", slotId);
                },
                () -> log.warn("Slot {} not found for release", slotId)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SlotDetailsResponse getSlotDetails(UUID slotId) {
        DoctorAvailableSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));

        return SlotDetailsResponse.builder()
                .slotId(slot.getId())
                .doctorId(slot.getDoctorId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(slot.isAvailable())
                .build();
    }

    private SlotStatusUpdateResponse updateSlotStatusWithValidation(UUID slotId, boolean isAvailable,
                                                                    String reason, ValidationType validationType) {

        DoctorAvailableSlot slot = findSlotWithDoctor(slotId);

        // Thực hiện validation phù hợp
        switch (validationType) {
            case RESERVATION:
                slotStatusValidationService.validateSlotReservation(slotId, slot);
                break;
            case RELEASE:
                slotStatusValidationService.validateSlotRelease(slotId, slot);
                break;
            case UPDATE:
                slotStatusValidationService.validateSlotAvailabilityUpdate(slotId, slot, isAvailable);
                break;
        }

        // Cập nhật và lưu slot
        return saveAndBuildResponse(slot, isAvailable, reason);
    }

    private SlotStatusUpdateResponse updateSlotStatus(UUID slotId, boolean isAvailable, String reason) {
        DoctorAvailableSlot slot = slotStatusValidationService.findAndValidateSlotForUpdate(slotId, isAvailable);

        return saveAndBuildResponse(slot, isAvailable, reason);

    }

    private DoctorAvailableSlot findSlotWithDoctor(UUID slotId) {
        return slotStatusRepository.findByIdWithDoctor(slotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));
    }

    private SlotStatusUpdateResponse buildSlotStatusUpdateResponse(DoctorAvailableSlot slot, String message) {
        return SlotStatusUpdateResponse.builder()
                .slotId(slot.getId())
                //.doctorId(slot.getDoctor().getId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.isAvailable())
                .message(message)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private SlotStatusUpdateResponse saveAndBuildResponse(DoctorAvailableSlot slot, boolean isAvailable, String reason) {
        boolean oldStatus = slot.isAvailable();
        slot.setAvailable(isAvailable);

        log.debug("Slot {} thay đổi trạng thái từ {} sang {}", slot.getId(), oldStatus, isAvailable);

        DoctorAvailableSlot updatedSlot = slotStatusRepository.save(slot);
        return buildSlotStatusUpdateResponse(updatedSlot, reason);
    }


    private void validateSlotForReservation(DoctorAvailableSlot slot,
                                            SlotReservationRequest request) {
        if (!slot.getDoctorId().equals(request.getDoctorId())) {
            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
        }

        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
        if (slotDateTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.SLOT_IN_PAST);
        }
    }
}
