package org.project.service;


import org.project.dto.request.SlotGenerationRequest;
import org.project.dto.response.SlotGenerationResponse;

public interface SlotGenerationService {

    SlotGenerationResponse generateSlots(SlotGenerationRequest request);
}
