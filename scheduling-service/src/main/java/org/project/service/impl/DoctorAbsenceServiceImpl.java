package org.project.service.impl;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.PageResponse;
import org.project.dto.request.BatchSlotStatusRequest;
import org.project.dto.request.CreateAbsenceRequest;
import org.project.dto.request.UpdateAbsenceRequest;
import org.project.dto.response.DoctorAbsenceResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.mapper.DoctorAbsenceMapper;
import org.project.mapper.PageMapper;
import org.project.model.DoctorAbsence;
import org.project.model.DoctorAvailableSlot;
import org.project.repository.DoctorAbsenceRepository;
import org.project.repository.DoctorAvailableSlotRepository;
import org.project.service.DoctorAbsenceService;
import org.project.service.SlotStatusService;
import org.project.validator.AbsenceValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DoctorAbsenceServiceImpl implements DoctorAbsenceService {

    DoctorAbsenceRepository doctorAbsenceRepository;
    DoctorAbsenceMapper doctorAbsenceMapper;
    AbsenceValidator absenceValidator;
    PageMapper pageMapper;


//    AppointmentRepository appointmentRepository;
//    PaymentRepository paymentRepository;
//    PaymentService paymentService;
    SlotStatusService slotStatusService;
    DoctorAvailableSlotRepository doctorAvailableSlotRepository;

    /**
     - Tạo lịch nghỉ mới cho bác sĩ
     - Xử lý các lịch hẹn bị ảnh hưởng
     */
//    @Override
//    public DoctorAbsenceResponse createAbsence(CreateAbsenceRequest request) {
//        absenceValidator.validateCreateRequest(request);
//
//        // Kiểm tra xung đột với lịch nghỉ hiện có
//        if (hasConflictingAbsence(request.getDoctorUserId(), request.getAbsenceDate(),
//                request.getStartTime(), request.getEndTime(), null)) {
//            throw new CustomException(ErrorCode.ABSENCE_CONFLICT);
//        }
//
//        DoctorAbsence absence = doctorAbsenceMapper.toEntity(request);
//        DoctorAbsence savedAbsence = doctorAbsenceRepository.save(absence);
//
//        log.info("Đã tạo lịch nghỉ mới với ID: {}", savedAbsence.getId());
//        handleAffectedAppointmentsAndSlots(savedAbsence);
//        return doctorAbsenceMapper.toDto(savedAbsence);
//    }

//    @Override
//    public DoctorAbsenceResponse updateAbsence(UUID absenceId, UpdateAbsenceRequest request) {
//        absenceValidator.validateUpdateRequest(request);
//
//        DoctorAbsence existingAbsence = getAbsenceEntityById(absenceId);
//
//        validateOwnership(existingAbsence.getDoctor().getId());
//
//        if (request.getAbsenceDate() != null || request.getStartTime() != null || request.getEndTime() != null) {
//            LocalDate newDate = request.getAbsenceDate() != null ?
//                    request.getAbsenceDate() : existingAbsence.getAbsenceDate();
//            LocalTime newStartTime = request.getStartTime() != null ?
//                    request.getStartTime() : existingAbsence.getStartTime();
//            LocalTime newEndTime = request.getEndTime() != null ?
//                    request.getEndTime() : existingAbsence.getEndTime();
//
//            if (hasConflictingAbsence(existingAbsence.getDoctor().getId(), newDate,
//                    newStartTime, newEndTime, absenceId)) {
//               throw new CustomException(ErrorCode.ABSENCE_CONFLICT);
//            }
//        }
//        doctorAbsenceMapper.updateEntityFromRequest(request, existingAbsence);
//        DoctorAbsence savedAbsence = doctorAbsenceRepository.save(existingAbsence);
//
//        log.info("Đã cập nhật lịch nghỉ: {}", absenceId);
//
//        handleAffectedAppointmentsAndSlots(savedAbsence);
//
//        return doctorAbsenceMapper.toDto(savedAbsence);
//    }

    @Override
    public DoctorAbsenceResponse getAbsenceById(UUID absenceId) {
        DoctorAbsence absence = getAbsenceEntityById(absenceId);
        return doctorAbsenceMapper.toDto(absence);
    }

    @Override
    public PageResponse<DoctorAbsenceResponse> getAbsencesByDoctor(UUID doctorUserId, Pageable pageable) {
        Page<DoctorAbsence> absences = doctorAbsenceRepository
                .findByDoctorIdOrderByAbsenceDateDesc(doctorUserId, pageable);
        return pageMapper.toPageResponse(absences, doctorAbsenceMapper::toDto);
    }

    @Override
    public List<DoctorAbsenceResponse> getAbsencesInDateRange(UUID doctorUserId, LocalDate startDate, LocalDate endDate) {
        List<DoctorAbsence> absences = doctorAbsenceRepository
                .findAbsencesInDateRange(doctorUserId, startDate, endDate);
        return doctorAbsenceMapper.toDtoList(absences);
    }

    @Override
    public List<DoctorAbsenceResponse> getFutureAbsences(UUID doctorUserId) {
        List<DoctorAbsence> absences = doctorAbsenceRepository
                .findFutureAbsences(doctorUserId, LocalDate.now());
        return doctorAbsenceMapper.toDtoList(absences);
    }

    @Override
    public void deleteAbsence(UUID absenceId) {
        DoctorAbsence absence = getAbsenceEntityById(absenceId);
        doctorAbsenceRepository.delete(absence);
        log.info("Đã xóa lịch nghỉ: {}", absenceId);
    }

    @Override
    public boolean isDoctorAbsentOnDate(UUID doctorUserId, LocalDate date) {
        List<DoctorAbsence> absences = doctorAbsenceRepository
                .findAbsencesInDateRange(doctorUserId, date, date);
        return !absences.isEmpty();
    }

    @Override
    public int cleanupPastAbsences(LocalDate cutoffDate) {
        int deletedCount = doctorAbsenceRepository.deletePastAbsences(cutoffDate);
        log.info("Đã xóa {} lịch nghỉ trong quá khứ", deletedCount);
        return deletedCount;
    }

//    private void handleAffectedAppointmentsAndSlots(DoctorAbsence absence) {
//        if (absence.getAbsenceDate().isBefore(LocalDate.now())) {
//            log.info("Ngày nghỉ {} đã qua, bỏ qua việc xử lý appointments và slots", absence.getAbsenceDate());
//            return;
//        }
//
//        try {
//            //Tìm các appointments bị ảnh hưởng
//            List<Appointment> affectedAppointments = findAffectedAppointments(absence);
//
//            if (!affectedAppointments.isEmpty()) {
//                processAffectedAppointments(affectedAppointments, "Bác sĩ nghỉ đột suất");
//                log.info("Đã xử lý {} appointments bị ảnh hưởng", affectedAppointments.size());
//            }
//
//            //Đánh dấu tất cả slots bị ảnh hưởng thành reserved
//            markAffectedSlotsAsReserved(absence);
//
//        } catch (Exception e) {
//            log.error("Lỗi khi xử lý appointments và slots cho lịch nghỉ ID: {}. Chi tiết: {}",
//                    absence.getId(), e.getMessage(), e);
//            throw new CustomException(ErrorCode.ABSENCE_APPOINTMENT_PROCESSING_ERROR, e.getMessage());
//        }
//    }
//
//    private void processAffectedAppointments(List<Appointment> appointments, String reason) {
//        log.info("Đang xử lý {} appointments bị ảnh hưởng", appointments.size());
//
//        for (Appointment appointment : appointments) {
//            processAffectedAppointmentSafely(appointment, reason);
//        }
//    }

    // Đánh dấu các slots bị ảnh hưởng bởi lịch nghỉ thành reserved
//    private void markAffectedSlotsAsReserved(DoctorAbsence absence) {
//        List<DoctorAvailableSlot> affectedSlots = findAffectedSlots(absence);
//
//        if (affectedSlots.isEmpty()) {
//            log.info("Không có slot nào bị ảnh hưởng bởi lịch nghỉ");
//            return;
//        }
//
//        // Batch reserve tất cả slots bị ảnh hưởng
//        List<BatchSlotStatusRequest> reservationRequests = affectedSlots.stream()
//                .filter(DoctorAvailableSlot::isAvailable)
//                .map(slot -> BatchSlotStatusRequest.builder()
//                        .slotId(slot.getId())
//                        .isAvailable(false)
//                        .reason(String.format("Reserved due to doctor absence on %s",
//                                absence.getAbsenceDate()))
//                        .build())
//                .collect(Collectors.toList());
//
//        if (!reservationRequests.isEmpty()) {
//            try {
//                slotStatusService.updateMultipleSlotStatus(reservationRequests);
//                log.info("Đã reserve {} slots do lịch nghỉ bác sĩ", reservationRequests.size());
//            } catch (Exception e) {
//                log.error("Lỗi khi reserve slots cho lịch nghỉ: {}", e.getMessage(), e);
//            }
//        }
//    }

//    private List<DoctorAvailableSlot> findAffectedSlots(DoctorAbsence absence) {
//        UUID doctorId = absence.getDoctor().getId();
//        LocalDate absenceDate = absence.getAbsenceDate();
//        LocalTime startTime = absence.getStartTime();
//        LocalTime endTime = absence.getEndTime();
//
//        // Trường hợp nghỉ cả ngày (startTime và endTime đều null)
//        if (startTime == null && endTime == null) {
//            log.info("Tìm tất cả slots trong ngày {} cho bác sĩ {}", absenceDate, doctorId);
//            return doctorAvailableSlotRepository.findByDoctorUserIdAndSlotDate(doctorId, absenceDate);
//        }
//
//        // Trường hợp nghỉ trong khoảng thời gian cụ thể
//        if (startTime != null && endTime != null) {
//            log.info("Tìm slots từ {} đến {} ngày {} cho bác sĩ {}",
//                    startTime, endTime, absenceDate, doctorId);
//            return doctorAvailableSlotRepository.findByDoctorUserIdAndSlotDateAndTimeRange(
//                    doctorId, absenceDate, startTime, endTime);
//        }
//
//        // Trường hợp chỉ có startTime (nghỉ từ thời điểm đó đến cuối ngày)
//        if (startTime != null) {
//            log.info("Tìm slots từ {} đến cuối ngày {} cho bác sĩ {}",
//                    startTime, absenceDate, doctorId);
//            return doctorAvailableSlotRepository.findByDoctorUserIdAndSlotDateAndStartTimeAfter(
//                    doctorId, absenceDate, startTime);
//        }
//
//        // Trường hợp chỉ có endTime (nghỉ từ đầu ngày đến thời điểm đó)
//        if (endTime != null) {
//            log.info("Tìm slots từ đầu ngày đến {} ngày {} cho bác sĩ {}",
//                    endTime, absenceDate, doctorId);
//            return doctorAvailableSlotRepository.findByDoctorUserIdAndSlotDateAndEndTimeBefore(
//                    doctorId, absenceDate, endTime);
//        }
//
//        return Collections.emptyList();
//    }

    // Tìm các appointment bị ảnh hưởng
//    private List<Appointment> findAffectedAppointments(DoctorAbsence absence) {
//        UUID doctorId = absence.getDoctor().getId();
//        LocalDate absenceDate = absence.getAbsenceDate();
//        LocalTime startTime = absence.getStartTime();
//        LocalTime endTime = absence.getEndTime();
//
//        List<Status> activeStatuses = Arrays.asList(Status.PENDING, Status.CONFIRMED);
//        if (startTime == null && endTime == null) {
//            log.debug("Tìm appointments cả ngày: {}", absenceDate);
//
//            return appointmentRepository.findAppointmentsByDoctorAndFullDayOptimal(
//                    doctorId, absenceDate, activeStatuses);
//        } else {
//            // Trường hợp nghỉ trong khoảng thời gian
//            LocalTime actualStartTime = startTime != null ? startTime : LocalTime.MIN;
//            LocalTime actualEndTime = endTime != null ? endTime : LocalTime.MAX;
//
//            log.debug("Tìm appointments từ {} đến {} trong ngày {}",
//                    actualStartTime, actualEndTime, absenceDate);
//
//            // Sử dụng method tìm appointments overlap
//            return appointmentRepository.findOverlappingAppointments(
//                    doctorId, absenceDate, actualStartTime, actualEndTime, activeStatuses);
//        }
//    }

    // Xử lý hủy lịch hẹn của từng appointment
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void processAffectedAppointmentSafely(Appointment appointment, String reason) {
//        try {
//            processAffectedAppointment(appointment, reason);
//        } catch (Exception e) {
//            log.error("Xử lý lịch hẹn ID: {} thất bại. Lỗi: {}",
//                    appointment.getId(), e.getMessage(), e);
//        }
//    }

    /**
     - Xử lý lịch hẹn bị ảnh hưởng bởi lịch nghỉ của bác sĩ
     - Hủy lịch hẹn
     - Hoàn tiền cho các thanh toán qua VNPAY
     */
//    private void processAffectedAppointment(Appointment appointment, String reason) {
//        try {
//            log.info("Đang xử lý lịch hẹn ID: {}", appointment.getId());
//
//            // Hủy lịch hẹn
//            appointment.setStatus(Status.CANCELLED);
//            appointment.setNotes(appointment.getNotes() + " | Đã hủy do: " + reason);
//            appointmentRepository.save(appointment);
//
//            // Chỉ hoàn tiền cho các thanh toán qua VNPAY
//            refundEligiblePayments(appointment, reason);
//
//            log.info("Đã xử lý thành công lịch hẹn ID: {}", appointment.getId());
//
//        } catch (Exception e) {
//            log.error("Lỗi khi xử lý lịch hẹn ID: {}. Chi tiết: {}",
//                    appointment.getId(), e.getMessage(), e);
//           // throw new RuntimeException("Xử lý lịch hẹn thất bại: " + appointment.getId(), e);
//        }
//    }

    /**
     - Lấy danh sách thanh toán đã hoàn thành
     - Lọc ra các thanh toán qua VNPAY
     - Xử lý hoàn tiền từng giao dịch
     */
//    private void refundEligiblePayments(Appointment appointment, String reason) {
//        // Lấy tất cả thanh toán đã hoàn thành
//        List<Payment> completedPayments = paymentRepository.findValidPaymentsByAppointmentIdAndStatus(
//                appointment.getId(), List.of(PaymentStatus.COMPLETED));
//
//        if (completedPayments.isEmpty()) {
//            log.info("Không tìm thấy thanh toán nào cho lịch hẹn ID: {}", appointment.getId());
//            return;
//        }
//
//        // Lọc chỉ lấy thanh toán qua VNPAY
//        List<Payment> vnpayPayments = completedPayments.stream()
//                .filter(payment -> isVnpayPayment(payment))
//                .collect(Collectors.toList());
//
//        if (vnpayPayments.isEmpty()) {
//            log.info("Không tìm thấy thanh toán VNPAY nào cho lịch hẹn ID: {}", appointment.getId());
//            return;
//        }
//
//        log.info("Tìm thấy {} thanh toán VNPAY cần hoàn tiền cho lịch hẹn ID: {}",
//                vnpayPayments.size(), appointment.getId());
//
//        for (Payment payment : vnpayPayments) {
//            refundPaymentSafely(payment, appointment, reason);
//        }
//    }

    /**
     - Xử lý hoàn tiền cho từng giao dịch
     */
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void refundPaymentSafely(Payment payment, Appointment appointment, String reason) {
//        try {
//            PaymentRefundRequest refundRequest = PaymentRefundRequest.builder()
//                    .paymentId(payment.getId())
//                    .appointmentId(appointment.getId())
//                    .reason(reason + " - Hoàn tiền 100%")
//                    .refundType(RefundType.FULL_REFUND)
//                    .build();
//
//            paymentService.refundPayment(refundRequest);
//
//            log.info("Đã hoàn tiền thành công cho thanh toán ID: {} của lịch hẹn ID: {}, số tiền: {}",
//                    payment.getId(), appointment.getId(), payment.getAmount());
//
//        } catch (Exception e) {
//            log.error("Hoàn tiền thất bại cho thanh toán ID: {} của lịch hẹn ID: {}. Lỗi: {}",
//                    payment.getId(), appointment.getId(), e.getMessage(), e);
//        }
//    }
//
//    private boolean isVnpayPayment(Payment payment) {
//        // Kiểm tra phương thức thanh toán là VNPAY
//        return payment.getPaymentMethod() != null &&
//                "VNPAY".contains(payment.getPaymentMethod().name());
//    }

    private DoctorAbsence getAbsenceEntityById(UUID absenceId) {
        return doctorAbsenceRepository.findById(absenceId)
                .orElseThrow(() -> new CustomException(ErrorCode.ABSENCE_NOT_FOUND));
    }

    private boolean hasConflictingAbsence(UUID doctorUserId, LocalDate absenceDate,
                                          LocalTime startTime, LocalTime endTime, UUID excludeId) {
        return doctorAbsenceRepository.existsConflictingAbsence(
                doctorUserId, absenceDate, startTime, endTime, excludeId
        );
    }

//    private void validateOwnership(UUID doctorUserId) {
//        UUID currentUserId = securityUtils.getCurrentUserId();
//
//        if (securityUtils.isCurrentUserAdmin()) {
//            return;
//        }
//
//        if (!currentUserId.equals(doctorUserId)) {
//            log.warn("Người dùng {} đã cố gắng chỉnh sửa lịch nghỉ của bác sĩ {}", currentUserId, doctorUserId);
//            throw new CustomException(ErrorCode.FORBIDDEN);
//        }
//    }
}
