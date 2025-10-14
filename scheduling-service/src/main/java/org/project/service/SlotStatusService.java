package org.project.service;



import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.dto.response.SlotStatusUpdateResponse;

import java.util.List;
import java.util.UUID;

public interface SlotStatusService {

    // Cập nhật trạng thái của một slot
    SlotStatusUpdateResponse markSlotAvailable(UUID slotId);

    // Đánh dấu slot là unavailable
    SlotStatusUpdateResponse markSlotUnavailable(UUID slotId);

    // Cập nhật trạng thái nhiều slots cùng lúc
    List<SlotStatusUpdateResponse> updateMultipleSlotStatus(List<BatchSlotStatusRequest> requests);

    // Đánh dấu slot là không khả dụng (reserved for booking)
    SlotStatusUpdateResponse reserveSlot(UUID slotId);

    // Giải phóng slot (make available again)
    SlotStatusUpdateResponse releaseSlot(UUID slotId);

    /**
     * Reserve slot với idempotency support (cho microservices)
     */
    SlotReservationResponse reserveSlotWithIdempotency(SlotReservationRequest request);

    /**
     * Release slot với idempotency key (compensation)
     */
    void releaseSlotWithIdempotency(UUID slotId, String idempotencyKey);

    /**
     * Confirm reservation sau khi appointment created
     */
    void confirmReservation(UUID slotId, String idempotencyKey);

    /**
     * Get slot details
     */
    SlotDetailsResponse getSlotDetails(UUID slotId);
}
