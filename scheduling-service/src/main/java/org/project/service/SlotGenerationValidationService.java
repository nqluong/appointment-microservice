package org.project.service;


import org.project.dto.request.SlotGenerationRequest;

public interface SlotGenerationValidationService {

    void validateRequest(SlotGenerationRequest request);
}
