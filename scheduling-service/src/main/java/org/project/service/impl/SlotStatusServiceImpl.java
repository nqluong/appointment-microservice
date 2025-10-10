package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.response.SlotStatusUpdateResponse;
import org.project.enums.ValidationType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.SlotStatusRepository;
import org.project.service.SlotStatusService;
import org.project.service.SlotStatusValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotStatusServiceImpl implements SlotStatusService {

    SlotStatusRepository slotStatusRepository;
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

    @Override
    @Transactional
    public SlotStatusUpdateResponse reserveSlot(UUID slotId) {
        return updateSlotStatusWithValidation(slotId, false, "Reserved for appointment booking",
                ValidationType.RESERVATION);
    }

    @Override
    @Transactional
    public SlotStatusUpdateResponse releaseSlot(UUID slotId) {
        return updateSlotStatusWithValidation(slotId, true, "Released from reservation",
                ValidationType.RELEASE);
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

}
