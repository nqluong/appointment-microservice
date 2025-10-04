package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.SlotGenerationRequest;
import org.project.dto.response.SlotGenerationResponse;
import org.project.repository.SlotGenerationRepository;
import org.project.service.SlotGenerationService;
import org.project.service.SlotGenerationValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotGenerationServiceImpl implements SlotGenerationService {

    SlotGenerationValidationService validationService;
    SlotGenerationRepository slotGenerationRepository;

    @Override
    @Transactional
    public SlotGenerationResponse generateSlots(SlotGenerationRequest request) {
        validationService.validateRequest(request);

        slotGenerationRepository.generateSlotsForRange(request.getDoctorId(),
                request.getStartDate(), request.getEndDate());
        long totalSlots = slotGenerationRepository.countAvailableSlots(
                request.getDoctorId(), request.getStartDate(), request.getEndDate()
        );

        return SlotGenerationResponse.builder()
                .doctorId(request.getDoctorId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalSlotsGenerated((int) totalSlots)
                .message("Slots generated successfully")
                .build();
    }
}
