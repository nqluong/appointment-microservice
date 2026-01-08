package org.project.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.project.dto.request.BulkSlotGenerationRequest;
import org.project.dto.request.SlotGenerationRequest;
import org.project.dto.response.BulkSlotGenerationResponse;
import org.project.dto.response.SlotGenerationResponse;
import org.project.repository.SlotGenerationRepository;
import org.project.service.SlotGenerationService;
import org.project.service.SlotGenerationValidationService;
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

    @Override
    @Transactional
    public BulkSlotGenerationResponse generateSlotsForMultipleDoctors(BulkSlotGenerationRequest request) {
        log.info("Starting bulk slot generation for {} doctors from {} to {}",
                request.getDoctorIds().size(), request.getStartDate(), request.getEndDate());

        List<BulkSlotGenerationResponse.DoctorSlotResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        int totalSlotsGenerated = 0;

        for (UUID doctorId : request.getDoctorIds()) {
            try {
                SlotGenerationRequest singleRequest = SlotGenerationRequest.builder()
                        .doctorId(doctorId)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .build();

                validationService.validateRequest(singleRequest);

                slotGenerationRepository.generateSlotsForRange(doctorId,
                        request.getStartDate(), request.getEndDate());
                long doctorSlots = slotGenerationRepository.countAvailableSlots(
                        doctorId, request.getStartDate(), request.getEndDate()
                );

                results.add(BulkSlotGenerationResponse.DoctorSlotResult.builder()
                        .doctorId(doctorId)
                        .success(true)
                        .slotsGenerated((int) doctorSlots)
                        .build());

                successCount++;
                totalSlotsGenerated += (int) doctorSlots;
                log.info("Successfully generated {} slots for doctor {}", doctorSlots, doctorId);

            } catch (Exception e) {
                log.error("Failed to generate slots for doctor {}: {}", doctorId, e.getMessage(), e);
                results.add(BulkSlotGenerationResponse.DoctorSlotResult.builder()
                        .doctorId(doctorId)
                        .success(false)
                        .slotsGenerated(0)
                        .errorMessage(e.getMessage())
                        .build());
                failureCount++;
            }
        }

        String message = String.format("Bulk generation completed: %d successful, %d failed out of %d doctors",
                successCount, failureCount, request.getDoctorIds().size());

        log.info(message);

        return BulkSlotGenerationResponse.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .totalDoctors(request.getDoctorIds().size())
                .successfulGenerations(successCount)
                .failedGenerations(failureCount)
                .totalSlotsGenerated(totalSlotsGenerated)
                .results(results)
                .message(message)
                .build();
    }
}
