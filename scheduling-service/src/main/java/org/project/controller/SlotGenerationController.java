package org.project.controller;

import org.project.dto.request.BulkSlotGenerationRequest;
import org.project.dto.request.SlotGenerationRequest;
import org.project.dto.response.BulkSlotGenerationResponse;
import org.project.dto.response.SlotGenerationResponse;
import org.project.service.SlotGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/schedules/slots")
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationController {

    private final SlotGenerationService slotGenerationService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('DOCTOR') OR hasRole('ADMIN')")
    public ResponseEntity<SlotGenerationResponse> generateSlots(
            @Valid @RequestBody SlotGenerationRequest request) {

        SlotGenerationResponse response = slotGenerationService.generateSlots(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/generate/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BulkSlotGenerationResponse> generateSlotsForMultipleDoctors(
            @Valid @RequestBody BulkSlotGenerationRequest request) {
        log.info("Received bulk slot generation request for {} doctors", request.getDoctorIds().size());

        BulkSlotGenerationResponse response = slotGenerationService.generateSlotsForMultipleDoctors(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
