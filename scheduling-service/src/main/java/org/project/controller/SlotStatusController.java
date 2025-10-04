package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.response.SlotStatusUpdateResponse;
import org.project.service.SlotStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules/slots")
@RequiredArgsConstructor
@Slf4j
public class SlotStatusController {

    private final SlotStatusService slotStatusService;

//    @PatchMapping("/{slotId}/available")
//    //@PreAuthorize("hasRole('DOCTOR') OR hasRole('ADMIN')")
//    public ResponseEntity<SlotStatusUpdateResponse> updateAvailableSlotStatus(
//            @PathVariable UUID slotId ){
//
//        SlotStatusUpdateResponse response = slotStatusService.markSlotAvailable(slotId);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PatchMapping("/{slotId}/unavailable")
//   // @PreAuthorize("hasRole('DOCTOR') OR hasRole('ADMIN')")
//    public ResponseEntity<SlotStatusUpdateResponse> updateUnavailableSlotStatus(
//            @PathVariable UUID slotId ){
//
//        SlotStatusUpdateResponse response = slotStatusService.markSlotUnavailable(slotId);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PutMapping("/batch-update")
//   // @PreAuthorize("hasRole('DOCTOR') OR hasRole('ADMIN')")
//    public ResponseEntity<List<SlotStatusUpdateResponse>> updateMultipleSlotStatus(
//            @Valid @RequestBody List<BatchSlotStatusRequest> requests) {
//
//        List<SlotStatusUpdateResponse> responses = slotStatusService
//                .updateMultipleSlotStatus(requests);
//
//        return ResponseEntity.ok(responses);
//    }

//    @PatchMapping("/{slotId}/reserve")
//    //@PreAuthorize("hasRole('DOCTOR') OR hasRole('ADMIN') OR hasRole('PATIENT')")
//    public ResponseEntity<SlotStatusUpdateResponse> reserveSlot(@PathVariable UUID slotId) {
//
//        SlotStatusUpdateResponse response = slotStatusService.reserveSlot(slotId);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PatchMapping("/{slotId}/release")
//    @PreAuthorize("hasRole('DOCTOR') OR hasRole('ADMIN')")
//    public ResponseEntity<SlotStatusUpdateResponse> releaseSlot(@PathVariable UUID slotId) {
//
//        SlotStatusUpdateResponse response = slotStatusService.releaseSlot(slotId);
//
//        return ResponseEntity.ok(response);
//    }
}
