package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.ConfirmReservationRequest;
import org.project.dto.request.SlotReleaseRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.service.SlotStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/slots")
@RequiredArgsConstructor
@Slf4j
public class InternalSlotReservationController {
    private final SlotStatusService slotStatusService;

    /**
     * Reserve slot (được gọi từ Appointment Service)
     */
    @PostMapping("/reserve")
    public ResponseEntity<SlotReservationResponse> reserveSlot(
            @Valid @RequestBody SlotReservationRequest request) {

        log.info("Received slot reservation request for slot {}", request.getSlotId());

        SlotReservationResponse response = slotStatusService.reserveSlotWithIdempotency(request);

        return ResponseEntity.ok(response);
    }

    /**
     * Release slot (compensation từ Appointment Service)
     */
    @PostMapping("/release")
    public ResponseEntity<Void> releaseSlot(
            @Valid @RequestBody SlotReleaseRequest request) {

        log.info("Received slot release request for slot {}", request.getSlotId());

        slotStatusService.releaseSlotWithIdempotency(request.getSlotId(), request.getIdempotencyKey());

        return ResponseEntity.ok().build();
    }

    /**
     * Confirm reservation (sau khi appointment created)
     */
    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmReservation(
            @Valid @RequestBody ConfirmReservationRequest request) {

        log.info("Confirming reservation for slot {}", request.getSlotId());

        slotStatusService.confirmReservation(request.getSlotId(), request.getIdempotencyKey());

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
