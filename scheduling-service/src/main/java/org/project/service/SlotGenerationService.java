package org.project.service;


import org.project.dto.request.BulkSlotGenerationRequest;
import org.project.dto.request.SlotGenerationRequest;
import org.project.dto.response.BulkSlotGenerationResponse;
import org.project.dto.response.SlotGenerationResponse;

public interface SlotGenerationService {

    SlotGenerationResponse generateSlots(SlotGenerationRequest request);

    BulkSlotGenerationResponse generateSlotsForMultipleDoctors(BulkSlotGenerationRequest request);
}
