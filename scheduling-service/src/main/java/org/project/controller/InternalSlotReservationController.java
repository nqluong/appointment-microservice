package org.project.controller;

import java.util.UUID;

import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.service.SlotStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/internal/slots")
@RequiredArgsConstructor
@Slf4j
public class InternalSlotReservationController {
    private final SlotStatusService slotStatusService;

    /**
     * Reserve slot (SYNC call từ Appointment Service)
     */
    @PostMapping("/reserve")
    public ResponseEntity<SlotReservationResponse> reserveSlot(
            @Valid @RequestBody SlotReservationRequest request) {

        log.info("Received slot reservation request for slot {}", request.getSlotId());

        SlotReservationResponse response = slotStatusService.reserveSlot(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Release slot (compensation từ Appointment Service)
     */
    @PostMapping("/release/{slotId}")
    public ResponseEntity<Void> releaseSlot(@PathVariable UUID slotId) {

        log.info("Received slot release request for slot {}", slotId);

        slotStatusService.releaseSlot(slotId);

        return ResponseEntity.ok().build();
    }

    /**
     * Get slot details
     */
    @GetMapping("/{slotId}")
    public ResponseEntity<SlotDetailsResponse> getSlotDetails(@PathVariable UUID slotId) {

        log.debug("Getting slot details for {}", slotId);

        SlotDetailsResponse response = slotStatusService.getSlotDetails(slotId);

        return ResponseEntity.ok(response);
    }
}
