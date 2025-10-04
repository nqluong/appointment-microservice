package org.project.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.SlotGenerationRequest;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.SlotGenerationValidationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SlotGenerationValidationServiceImpl implements SlotGenerationValidationService {

    //UserRepository userRepository;

    @Override
    public void validateRequest(SlotGenerationRequest request) {
        validateDates(request);
        validateDoctor(request);
    }

    private void validateDates(SlotGenerationRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "Start date must be before or equal to end date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "Cannot generate slots for past dates");
        }


        if (request.getStartDate().plusDays(90).isBefore(request.getEndDate())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "Date range cannot exceed 90 days");
        }
    }

    private void validateDoctor(SlotGenerationRequest request) {
//        if (!userRepository.existsById(request.getDoctorId())) {
//            throw new CustomException(ErrorCode.USER_NOT_FOUND,
//                    "Doctor not found with ID: " + request.getDoctorId());
//        }

        // Xác thực xem user có quyền DOCTOR
//        boolean isDoctor = userRepository.findById(request.getDoctorId())
//                .map(user -> user.getUserRoles().stream()
//                        .anyMatch(userRole -> "DOCTOR".equals(userRole.getRole().getName())))
//                .orElse(false);
//
//        if (!isDoctor) {
//            throw new CustomException(ErrorCode.ACCESS_DENIED,
//                    "User is not a doctor");
//        }
    }
}
