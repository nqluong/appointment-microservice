package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.SlotGenerationRequest;
import org.project.dto.response.SlotGenerationResponse;
import org.project.service.SlotGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
