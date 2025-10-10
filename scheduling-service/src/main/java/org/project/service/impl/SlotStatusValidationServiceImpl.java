package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.BatchSlotStatusRequest;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.SlotStatusRepository;
import org.project.service.SlotStatusValidationService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SlotStatusValidationServiceImpl implements SlotStatusValidationService {

    SlotStatusRepository slotStatusRepository;

    // Validate slot cho việc cập nhật trạng thái
    @Override
    public DoctorAvailableSlot findAndValidateSlotForUpdate(UUID slotId, boolean newStatus) {
        DoctorAvailableSlot slot = findSlotWithDoctor(slotId);
        validateSlotAvailabilityUpdate(slotId, slot, newStatus);
        return slot;
    }

    @Override
    public void validateSlotAvailabilityUpdate(UUID slotId, DoctorAvailableSlot slot, boolean newStatus) {
        // Validate các điều kiện cơ bản của slot
        validateBasicSlotConditions(slot);

        // Kiểm tra xem trạng thái có thực sự thay đổi không
        if (slot.isAvailable() == newStatus) {
            if (newStatus) {
                throw new CustomException(ErrorCode.SLOT_ALREADY_AVAILABLE);
            } else {
                throw new CustomException(ErrorCode.SLOT_ALREADY_RESERVED);
            }
        }
    }


    @Override
    public void validateMultipleSlotStatusUpdate(List<BatchSlotStatusRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            throw new CustomException(ErrorCode.INVALID_SLOT_OPERATION, "Request list cannot be empty");
        }

        if (requests.size() > 50) {
            throw new CustomException(ErrorCode.BULK_OPERATION_LIMIT_EXCEEDED,
                    "Cannot update more than 50 slots at once");
        }

        long distinctSlotIds = requests.stream()
                .map(BatchSlotStatusRequest::getSlotId)
                .distinct()
                .count();

        if (distinctSlotIds != requests.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_SLOT_IDS);
        }
    }

    @Override
    public void validateSlotReservation(UUID slotId, DoctorAvailableSlot slot) {
        validateBasicSlotConditions(slot);

        if (!slot.isAvailable()) {
            throw new CustomException(ErrorCode.SLOT_ALREADY_RESERVED);

        }
    }

    @Override
    public void validateSlotRelease(UUID slotId, DoctorAvailableSlot slot) {
        validateSlotExists(slot);
    }

    @Override
    public void validateSlotOperation(UUID slotId, DoctorAvailableSlot slot) {
        validateBasicSlotConditions(slot);
    }


    // Validate các điều kiện cơ bản của slot (tồn tại và không ở quá khứ)
    private void validateBasicSlotConditions(DoctorAvailableSlot slot) {
        validateSlotExists(slot);
        validateSlotNotInPast(slot);
    }

    private DoctorAvailableSlot findSlotWithDoctor(UUID slotId) {
        return slotStatusRepository.findByIdWithDoctor(slotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));
    }

    // Kiểm tra slot có tồn tại không
    private void validateSlotExists(DoctorAvailableSlot slot) {
        if (slot == null) {
            throw new CustomException(ErrorCode.SLOT_NOT_FOUND);
        }
    }

    private void validateSlotNotInPast(DoctorAvailableSlot slot) {
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());

        if (slotDateTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.SLOT_IN_PAST);
        }
    }

}
