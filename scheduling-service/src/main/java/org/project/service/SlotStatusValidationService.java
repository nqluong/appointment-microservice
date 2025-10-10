package org.project.service;



import org.project.dto.request.BatchSlotStatusRequest;
import org.project.model.DoctorAvailableSlot;

import java.util.List;
import java.util.UUID;

public interface SlotStatusValidationService {
    // Validate slot cho việc cập nhật trạng thái
    DoctorAvailableSlot findAndValidateSlotForUpdate(UUID slotId, boolean newStatus);

    // Validate việc cập nhật trạng thái availability của slot
    void validateSlotAvailabilityUpdate(UUID slotId, DoctorAvailableSlot slot, boolean newStatus);

    // Validate việc cập nhật trạng thái của nhiều slot cùng lúc
    void validateMultipleSlotStatusUpdate(List<BatchSlotStatusRequest> requests);

    // Validate việc đặt trước slot
    void validateSlotReservation(UUID slotId, DoctorAvailableSlot slot);

    // Validate việc giải phóng slot
    void validateSlotRelease(UUID slotId, DoctorAvailableSlot slot);

    void validateSlotOperation(UUID slotId, DoctorAvailableSlot slot);
}
