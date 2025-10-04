package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.PageResponse;
import org.project.dto.request.DoctorScheduleCreateRequest;
import org.project.dto.request.DoctorSearchRequest;
import org.project.dto.response.DoctorScheduleResponse;
import org.project.dto.response.DoctorSearchResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.mapper.DoctorScheduleMapper;
import org.project.mapper.PageMapper;
import org.project.model.DoctorSchedule;
import org.project.repository.DoctorScheduleRepository;
import org.project.service.DoctorScheduleService;
import org.project.service.DoctorSearchSpecificationService;
import org.project.service.ScheduleValidationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    DoctorScheduleRepository doctorScheduleRepository;
   // UserRepository userRepository;
    DoctorScheduleMapper doctorScheduleMapper;
   // DoctorSearchMapper doctorSearchMapper;
    PageMapper pageMapper;
    ScheduleValidationService scheduleValidationService;
   // DoctorSearchSpecificationService specificationService;

//    @Override
//    @Transactional
//    public DoctorScheduleResponse createDoctorSchedule(DoctorScheduleCreateRequest request) {
//        // Validate doctor và có quyền phù hợp không
//        User doctor = findAndValidateDoctor(request.getDoctorId());
//
//        // Validate schedule
//        scheduleValidationService.validateScheduleEntries(request.getScheduleEntries());
//
//        // Lấy danh sách các ngày đã có lịch của bác sĩ
//        List<DoctorSchedule> existingSchedules = doctorScheduleRepository.findByDoctorIdAndIsActiveTrue(doctor.getId());
//        Set<Integer> existingDaysOfWeek = existingSchedules.stream()
//                .map(DoctorSchedule::getDayOfWeek)
//                .collect(Collectors.toSet());
//
//        // Kiểm tra xem có ngày nào trong yêu cầu đã có lịch chưa
//        boolean hasConflict = request.getScheduleEntries().stream()
//                .anyMatch(entry -> existingDaysOfWeek.contains(entry.getDayOfWeek()));
//
//        if (hasConflict) {
//            log.warn("Bác sĩ {} đã có lịch làm việc cho (các) ngày yêu cầu", doctor.getId());
//            throw new CustomException(ErrorCode.SCHEDULE_ALREADY_EXISTS);
//        }
//
//        List<DoctorSchedule> schedules = doctorScheduleMapper.toEntityList(
//                request.getScheduleEntries(),
//                doctor,
//                request.getTimezone(),
//                request.getNotes()
//        );
//
//        List<DoctorSchedule> savedSchedules = doctorScheduleRepository.saveAll(schedules);
//
//        log.info("Đã tạo thành công {} lịch làm việc cho bác sĩ: {}",
//                savedSchedules.size(), doctor.getId());
//
//        return doctorScheduleMapper.toDoctorScheduleResponse(doctor, savedSchedules);
//    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public DoctorScheduleResponse getDoctorSchedule(UUID doctorId) {
//        User doctor = findAndValidateDoctor(doctorId);
//        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorIdAndIsActiveTrue(doctorId);
//
//        if (schedules.isEmpty()) {
//            log.warn("Không tìm thấy lịch làm việc đang hoạt động nào cho bác sĩ: {}", doctorId);
//            throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
//        }
//
//        return doctorScheduleMapper.toDoctorScheduleResponse(doctor, schedules);
//    }

//    @Override
//    @Transactional
//    public DoctorScheduleResponse updateDoctorSchedule(UUID doctorId, DoctorScheduleUpdateRequest request) {
//        User doctor = findAndValidateDoctor(doctorId);
//        scheduleValidationService.validateScheduleEntries(request.getScheduleEntries());
//
//        // Lấy tất cả schedule hiện có của doctor
//        List<DoctorSchedule> existingSchedules = doctorScheduleRepository.findByDoctorId(doctorId);
//
//        // Tạo map để dễ dàng lookup schedule theo dayOfWeek
//        Map<Integer, DoctorSchedule> existingScheduleMap = existingSchedules.stream()
//                .collect(Collectors.toMap(DoctorSchedule::getDayOfWeek, Function.identity()));
//
//        List<DoctorSchedule> updatedSchedules = new ArrayList<>();
//
//        for (ScheduleEntryRequest entryRequest : request.getScheduleEntries()) {
//            DoctorSchedule existingSchedule = existingScheduleMap.get(entryRequest.getDayOfWeek());
//
//            if (existingSchedule == null) {
//                throw new CustomException(ErrorCode.INVALID_REQUEST, "No schedule found for day " + entryRequest.getDayOfWeek() + ".");
//            }
//
//            DoctorSchedule updatedSchedule = doctorScheduleMapper.updateEntity(
//                    existingSchedule,
//                    entryRequest,
//                    request.getTimezone(),
//                    request.getNotes()
//            );
//            updatedSchedules.add(updatedSchedule);
//
//            existingScheduleMap.remove(entryRequest.getDayOfWeek());
//        }
//
//        List<DoctorSchedule> savedSchedules = doctorScheduleRepository.saveAll(updatedSchedules);
//
//        // Lịch không được update
//        List<DoctorSchedule> unchangedSchedules = new ArrayList<>(existingScheduleMap.values());
//
//        // Tổng hợp lại và trả về
//        List<DoctorSchedule> activeSchedules = Stream.concat(
//                unchangedSchedules.stream(),
//                savedSchedules.stream().filter(DoctorSchedule::isActive)
//        ).collect(Collectors.toList());
//
//        log.info("Đã cập nhật thành công lịch làm việc cho bác sĩ: {}", doctorId);
//
//        return doctorScheduleMapper.toDoctorScheduleResponse(doctor, activeSchedules);
//    }


    @Override
    @Transactional
    public void deleteDoctorSchedule(UUID doctorId) {
       // findAndValidateDoctor(doctorId);

        int deactivatedCount = doctorScheduleRepository.deactivateSchedulesByDoctorId(doctorId);

        if (deactivatedCount == 0) {
            log.warn("Không tìm thấy lịch làm việc nào để xóa cho bác sĩ: {}", doctorId);
            throw new CustomException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        log.info("Đã vô hiệu hóa thành công {} lịch làm việc của bác sĩ: {}", deactivatedCount, doctorId);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public PageResponse<DoctorSearchResponse> searchDoctors(DoctorSearchRequest request) {
//        Specification<User> specification = specificationService.buildDoctorSearchSpecification(request);
//
//        Pageable pageable = PageRequest.of(
//                request.getPage(),
//                request.getSize(),
//                Sort.Direction.fromString(request.getSortDirection()),
//                request.getSortBy()
//        );
//
//        Page<User> doctorPage = userRepository.findAll(specification, pageable);
//
//        PageResponse<DoctorSearchResponse> response = pageMapper.toPageResponse(doctorPage, doctor -> {
//            List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorIdAndIsActiveTrue(doctor.getId());
//            return doctorSearchMapper.toDoctorSearchResponse(doctor, schedules);
//        });
//
//        log.debug("Tìm thấy {} bác sĩ phù hợp với tiêu chí tìm kiếm", response.getTotalElements());
//
//        return response;
//    }

//    private User findAndValidateDoctor(UUID doctorId) {
//        User doctor = userRepository.findById(doctorId)
//                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//
//        if (!isDoctorRole(doctor)) {
//            throw new CustomException(ErrorCode.INSUFFICIENT_PERMISSION);
//        }
//
//        return doctor;
//    }
//
//    private boolean isDoctorRole(User user) {
//        return user.getUserRoles().stream()
//                .anyMatch(userRole -> "DOCTOR".equals(userRole.getRole().getName()));
//    }


}
