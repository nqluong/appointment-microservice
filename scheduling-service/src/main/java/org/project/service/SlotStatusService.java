package org.project.service;



import java.util.List;
import java.util.UUID;

import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.dto.response.SlotStatusUpdateResponse;

public interface SlotStatusService {

    // Cập nhật trạng thái của một slot
    SlotStatusUpdateResponse markSlotAvailable(UUID slotId);

    // Đánh dấu slot là unavailable
    SlotStatusUpdateResponse markSlotUnavailable(UUID slotId);

    // Cập nhật trạng thái nhiều slots cùng lúc
    List<SlotStatusUpdateResponse> updateMultipleSlotStatus(List<BatchSlotStatusRequest> requests);

    /**
     * Sử dụng optimistic lock để handle race condition
     */
    SlotReservationResponse reserveSlot(SlotReservationRequest request);

    void releaseSlot(UUID slotId);

    SlotDetailsResponse getSlotDetails(UUID slotId);
}
