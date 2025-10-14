package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.request.SlotReservationRequest;
import org.project.dto.response.SlotDetailsResponse;
import org.project.dto.response.SlotReservationResponse;
import org.project.dto.response.SlotStatusUpdateResponse;
import org.project.enums.ValidationType;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.DoctorAvailableSlot;
import org.project.model.SlotReservation;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.repository.SlotReservationRepository;
import org.project.repository.SlotStatusRepository;
import org.project.service.SlotStatusService;
import org.project.service.SlotStatusValidationService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotStatusServiceImpl implements SlotStatusService {

    SlotStatusRepository slotStatusRepository;
    DoctorAvailableSlotRepository slotRepository;
    SlotStatusValidationService slotStatusValidationService;
    SlotReservationRepository slotReservationRepository;

    private static final int RESERVATION_TIMEOUT_MINUTES = 15;

    @Override
    public SlotStatusUpdateResponse markSlotAvailable(UUID slotId) {
        return updateSlotStatus(slotId, true, "Slot marked as available");
    }

    @Override
    public SlotStatusUpdateResponse markSlotUnavailable(UUID slotId) {
        return updateSlotStatus(slotId, false, "Slot marked as unavailable");
    }


    @Override
    @Transactional
    public List<SlotStatusUpdateResponse> updateMultipleSlotStatus(List<BatchSlotStatusRequest> requests) {
        slotStatusValidationService.validateMultipleSlotStatusUpdate(requests);

        return requests.stream()
                .map(request -> updateSlotStatus(
                        request.getSlotId(),
                        request.getIsAvailable(),
                        request.getReason() != null ? request.getReason() :
                                (request.getIsAvailable() ? "Batch update: available" : "Batch update: unavailable")
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SlotStatusUpdateResponse reserveSlot(UUID slotId) {
        return updateSlotStatusWithValidation(slotId, false, "Reserved for appointment booking",
                ValidationType.RESERVATION);
    }

    @Override
    @Transactional
    public SlotStatusUpdateResponse releaseSlot(UUID slotId) {
        return updateSlotStatusWithValidation(slotId, true, "Released from reservation",
                ValidationType.RELEASE);
    }

    @Override
    @Transactional
    public SlotReservationResponse reserveSlotWithIdempotency(SlotReservationRequest request) {
        log.info("Reserving slot {} with idempotency key {}",
                request.getSlotId(), request.getIdempotencyKey());

        // Step 1: Check idempotency - nếu đã reserve rồi thì return
        Optional<SlotReservation> existingReservation =
                slotReservationRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existingReservation.isPresent()) {
            SlotReservation reservation = existingReservation.get();

            if (reservation.isConfirmed()) {
                return SlotReservationResponse.builder()
                        .success(false)
                        .slotId(reservation.getSlotId())
                        .message("Slot đã được xác nhận cho appointment khác")
                        .build();
            }

            // Nếu reservation vẫn active và chưa hết hạn → Return success (idempotent)
            if (reservation.isActive() && reservation.getExpiresAt().isAfter(LocalDateTime.now())) {
                log.info("Slot {} đã được đặt chỗ với idempotency key {}",
                        reservation.getSlotId(), request.getIdempotencyKey());

                return SlotReservationResponse.builder()
                        .success(true)
                        .slotId(reservation.getSlotId())
                        .message("Slot đã được đặt chỗ trước đó (idempotent)")
                        .build();
            }
        }

        // Step 2: Clean expired reservations
        cleanupExpiredReservations();

        // Step 3: Get slot với optimistic lock
        DoctorAvailableSlot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));

        // Step 4: Validate slot
        validateSlotForReservation(slot, request);
        Optional<SlotReservation> activeReservation =
                slotReservationRepository.findActiveReservationBySlotId(
                        slot.getId(),
                        LocalDateTime.now()
                );

        // Step 5: Check slot đã được reserve chưa
        if (activeReservation.isPresent()) {
            SlotReservation existing = activeReservation.get();

            // Nếu là cùng 1 idempotency key → Idempotent (đã handle ở trên)
            if (existing.getIdempotencyKey().equals(request.getIdempotencyKey())) {
                return SlotReservationResponse.builder()
                        .success(true)
                        .slotId(existing.getSlotId())
                        .message("Slot đã được đặt chỗ trước đó")
                        .build();
            }

            if (existing.isConfirmed()) {
                return SlotReservationResponse.builder()
                        .success(false)
                        .slotId(slot.getId())
                        .message("Slot này đã có appointment được xác nhận")
                        .build();
            }

            // Nếu là người khác → Reject
            return SlotReservationResponse.builder()
                    .success(false)
                    .slotId(slot.getId())
                    .message("Slot này đã được đặt bởi bệnh nhân khác, vui lòng chọn slot khác")
                    .build();
        }

        try {
            // Step 6: Create reservation record
            SlotReservation reservation = SlotReservation.builder()
                    .slotId(slot.getId())
                    .patientId(request.getPatientId())
                    .idempotencyKey(request.getIdempotencyKey())
                    .reservedAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(RESERVATION_TIMEOUT_MINUTES))
                    .active(true)
                    .confirmed(false)
                    .build();

            slotReservationRepository.save(reservation);

            // Step 7: Update slot status
            slot.setAvailable(false);
            slot.setReservedBy(request.getPatientId());
            slot.setReservedAt(LocalDateTime.now());
            slotRepository.save(slot); // Optimistic lock

            log.info("Đặt chỗ slot {} thành công cho bệnh nhân {}",
                    slot.getId(), request.getPatientId());

            return SlotReservationResponse.builder()
                    .success(true)
                    .slotId(slot.getId())
                    .message("Đặt chỗ slot thành công")
                    .build();

        } catch (ObjectOptimisticLockingFailureException e) {

            return SlotReservationResponse.builder()
                    .success(false)
                    .slotId(slot.getId())
                    .message("Slot vừa được đặt bởi người dùng khác, vui lòng chọn slot khác")
                    .build();
        }
    }

    @Override
    @Transactional
    public void releaseSlotWithIdempotency(UUID slotId, String idempotencyKey) {
        log.info("Đang giải phóng slot {} với idempotency key {}", slotId, idempotencyKey);

        // Deactivate reservation
        Optional<SlotReservation> reservation =
                slotReservationRepository.findByIdempotencyKey(idempotencyKey);

        if (reservation.isPresent()) {

            SlotReservation res = reservation.get();
            if (res.isConfirmed()) {
                log.warn("Cannot release confirmed reservation {}", idempotencyKey);
                return;
            }

            if (!res.isActive()) {
                log.info("Reservation {} already released", idempotencyKey);
                return;
            }

            // Set active = false để cho phép người khác đặt
            res.setActive(false);
            res.setCancellationReason("Compensation - Appointment creation failed");
            slotReservationRepository.save(res);

            log.info("Đã deactivate reservation với key {}", idempotencyKey);
        } else {
            log.warn("Không tìm thấy reservation với idempotency key {}", idempotencyKey);
        }

        Optional<DoctorAvailableSlot> slotOpt = slotRepository.findById(slotId);
        if (slotOpt.isPresent()) {
            DoctorAvailableSlot slot = slotOpt.get();
            if (reservation.isPresent() &&
                    slot.getReservedBy() != null &&
                    slot.getReservedBy().equals(reservation.get().getPatientId())) {

                slot.setAvailable(true);
                slot.setReservedBy(null);
                slot.setReservedAt(null);
                slotRepository.save(slot);

                log.info("Slot {} đã được giải phóng", slotId);
            }

        } else {
            log.error("Không tìm thấy slot {} để giải phóng", slotId);
        }
    }

    @Override
    @Transactional
    public void confirmReservation(UUID slotId, String idempotencyKey) {
        slotReservationRepository.findByIdempotencyKey(idempotencyKey)
                .ifPresent(reservation -> {
                    reservation.setConfirmed(true);
                    reservation.setConfirmedAt(LocalDateTime.now());
                    slotReservationRepository.save(reservation);
                    log.info("Đã xác nhận reservation cho slot {}", slotId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public SlotDetailsResponse getSlotDetails(UUID slotId) {
        DoctorAvailableSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));

        return SlotDetailsResponse.builder()
                .slotId(slot.getId())
                .doctorId(slot.getDoctorId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .available(slot.isAvailable())
                .build();
    }

    private SlotStatusUpdateResponse updateSlotStatusWithValidation(UUID slotId, boolean isAvailable,
                                                                    String reason, ValidationType validationType) {

        DoctorAvailableSlot slot = findSlotWithDoctor(slotId);

        // Thực hiện validation phù hợp
        switch (validationType) {
            case RESERVATION:
                slotStatusValidationService.validateSlotReservation(slotId, slot);
                break;
            case RELEASE:
                slotStatusValidationService.validateSlotRelease(slotId, slot);
                break;
            case UPDATE:
                slotStatusValidationService.validateSlotAvailabilityUpdate(slotId, slot, isAvailable);
                break;
        }

        // Cập nhật và lưu slot
        return saveAndBuildResponse(slot, isAvailable, reason);
    }

    private SlotStatusUpdateResponse updateSlotStatus(UUID slotId, boolean isAvailable, String reason) {
        DoctorAvailableSlot slot = slotStatusValidationService.findAndValidateSlotForUpdate(slotId, isAvailable);

        return saveAndBuildResponse(slot, isAvailable, reason);

    }

    private DoctorAvailableSlot findSlotWithDoctor(UUID slotId) {
        return slotStatusRepository.findByIdWithDoctor(slotId)
                .orElseThrow(() -> new CustomException(ErrorCode.SLOT_NOT_FOUND));
    }

    private SlotStatusUpdateResponse buildSlotStatusUpdateResponse(DoctorAvailableSlot slot, String message) {
        return SlotStatusUpdateResponse.builder()
                .slotId(slot.getId())
                //.doctorId(slot.getDoctor().getId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .isAvailable(slot.isAvailable())
                .message(message)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private SlotStatusUpdateResponse saveAndBuildResponse(DoctorAvailableSlot slot, boolean isAvailable, String reason) {
        boolean oldStatus = slot.isAvailable();
        slot.setAvailable(isAvailable);

        log.debug("Slot {} thay đổi trạng thái từ {} sang {}", slot.getId(), oldStatus, isAvailable);

        DoctorAvailableSlot updatedSlot = slotStatusRepository.save(slot);
        return buildSlotStatusUpdateResponse(updatedSlot, reason);
    }

    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<SlotReservation> expiredReservations =
                slotReservationRepository.findExpiredReservations(now);

        for (SlotReservation reservation : expiredReservations) {
            reservation.setActive(false);
            reservation.setCancellationReason("Hết hạn reservation (15 phút)");
            slotReservationRepository.save(reservation);

            if (!reservation.isConfirmed()) {
                slotRepository.findById(reservation.getSlotId()).ifPresent(slot -> {
                    slot.setAvailable(true);
                    slot.setReservedBy(null);
                    slot.setReservedAt(null);
                    slotRepository.save(slot);
                });
            }
        }

        if (!expiredReservations.isEmpty()) {
            log.info("Cleaned up {} expired reservations", expiredReservations.size());
        }
    }

    private void validateSlotForReservation(DoctorAvailableSlot slot,
                                            SlotReservationRequest request) {
        if (!slot.getDoctorId().equals(request.getDoctorId())) {
            throw new CustomException(ErrorCode.INVALID_SLOT_DOCTOR);
        }

        LocalDateTime slotDateTime = LocalDateTime.of(slot.getSlotDate(), slot.getStartTime());
        if (slotDateTime.isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.SLOT_IN_PAST);
        }
    }
}
