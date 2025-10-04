package org.project.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.DoctorSearchRequest;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.service.SearchRequestValidator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class SearchRequestValidatorImpl implements SearchRequestValidator {

    @Override
    public void validateSearchRequest(DoctorSearchRequest request) {
        log.debug("Validating search request: {}", request);

        try {
            // Validate kinh nghiệm
            validateExperienceRange(request);

            // Validate giá khám
            validateConsultationFeeRange(request);

            // Validate thời gian làm việc
            validateTimeRange(request);

            // Validate pagination
            validatePagination(request);

        } catch (Exception e) {
            log.error("Validation failed for search request", e);
            throw new CustomException(ErrorCode.INVALID_SEARCH_CRITERIA, e.getMessage());
        }
    }

    private void validateExperienceRange(DoctorSearchRequest request) {
        if (request.getMinExperience() != null && request.getMinExperience() < 0) {
            throw new CustomException(ErrorCode.INVALID_EXPERIENCE_RANGE,
                    "Minimum experience cannot be negative");
        }

        if (request.getMaxExperience() != null && request.getMaxExperience() < 0) {
            throw new CustomException(ErrorCode.INVALID_EXPERIENCE_RANGE,
                    "Maximum experience cannot be negative");
        }

        if (request.getMinExperience() != null && request.getMaxExperience() != null
                && request.getMinExperience() > request.getMaxExperience()) {
            throw new CustomException(ErrorCode.INVALID_EXPERIENCE_RANGE,
                    "Minimum experience cannot be greater than maximum experience");
        }
    }

    private void validateConsultationFeeRange(DoctorSearchRequest request) {
        if (request.getMinConsultationFee() != null &&
                request.getMinConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorCode.INVALID_FEE_RANGE,
                    "Minimum consultation fee cannot be negative");
        }

        if (request.getMaxConsultationFee() != null &&
                request.getMaxConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException(ErrorCode.INVALID_FEE_RANGE,
                    "Maximum consultation fee cannot be negative");
        }

        if (request.getMinConsultationFee() != null && request.getMaxConsultationFee() != null
                && request.getMinConsultationFee().compareTo(request.getMaxConsultationFee()) > 0) {
            throw new CustomException(ErrorCode.INVALID_FEE_RANGE,
                    "Minimum fee cannot be greater than maximum fee");
        }
    }

    private void validateTimeRange(DoctorSearchRequest request) {
        if (request.getPreferredStartTime() != null && request.getPreferredEndTime() != null
                && request.getPreferredStartTime().isAfter(request.getPreferredEndTime())) {
            throw new CustomException(ErrorCode.INVALID_DATE_RANGE,
                    "Start time cannot be after end time");
        }
    }


    private void validatePagination(DoctorSearchRequest request) {
        if (request.getPage() < 0) {
            throw new CustomException(ErrorCode.INVALID_SEARCH_CRITERIA,
                    "Page number cannot be negative");
        }

        if (request.getSize() <= 0 || request.getSize() > 100) {
            throw new CustomException(ErrorCode.INVALID_SEARCH_CRITERIA,
                    "Page size must be between 1 and 100");
        }
    }
}
