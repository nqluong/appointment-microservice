//package org.project.service.impl;
//
//import jakarta.persistence.criteria.*;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import org.project.dto.request.DoctorSearchRequest;
//import org.project.exception.CustomException;
//import org.project.exception.ErrorCode;
//import org.project.service.DoctorSearchSpecificationService;
//import org.project.service.SearchRequestValidator;
//import org.springframework.data.jpa.domain.Specification;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//public class DoctorSearchSpecificationServiceImpl implements DoctorSearchSpecificationService {
//    SearchRequestValidator validator;
//
//    @Override
//    public Specification<User> buildDoctorSearchSpecification(DoctorSearchRequest request) {
//        validator.validateSearchRequest(request);
//
//        return (root, query, criteriaBuilder) -> {
//            try {
//                List<Predicate> predicates = new ArrayList<>();
//                JoinManager joinManager = new JoinManager(root);
//
//                // Luôn lọc user có role DOCTOR
//                addDoctorRoleFilter(root, criteriaBuilder, predicates);
//
//                // Apply các bộ lọc
//                applyFilters(request, joinManager, criteriaBuilder, predicates);
//
//                query.distinct(true);
//                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
//
//            } catch (Exception e) {
//                log.error("Error building search specification", e);
//                throw new CustomException(ErrorCode.DATABASE_ERROR,
//                        "Failed to build search query: " + e.getMessage());
//            }
//        };
//    }
//
//    private void applyFilters(DoctorSearchRequest request, JoinManager joinManager,
//                              CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//
//        // Lọc theo tên bác sĩ
//        if (StringUtils.hasText(request.getDoctorName())) {
//            addDoctorNameFilter(request.getDoctorName(), joinManager.getUserProfileJoin(),
//                    criteriaBuilder, predicates);
//        }
//
//        // Lọc theo chuyên khoa
//        if (hasSpecialtyFilter(request)) {
//            addSpecialtyFilter(request, joinManager.getSpecialtyJoin(), criteriaBuilder, predicates);
//        }
//
//        // Lọc theo trạng thái phê duyệt
//        if (request.getIsApproved() != null) {
//            addApprovalStatusFilter(request.getIsApproved(), joinManager.getMedicalJoin(),
//                    criteriaBuilder, predicates);
//        }
//
//        // Lọc theo kinh nghiệm
//        if (hasExperienceFilter(request)) {
//            addExperienceFilter(request, joinManager.getMedicalJoin(), criteriaBuilder, predicates);
//        }
//
//        // Lọc theo giá khám
//        if (hasConsultationFeeFilter(request)) {
//            addConsultationFeeFilter(request, joinManager.getMedicalJoin(), criteriaBuilder, predicates);
//        }
//
//        // Lọc theo bằng cấp
//        if (StringUtils.hasText(request.getQualification())) {
//            addQualificationFilter(request.getQualification(), joinManager.getMedicalJoin(),
//                    criteriaBuilder, predicates);
//        }
//
//        // Lọc theo lịch làm việc
//        if (request.getAvailableDate() != null) {
//            addAvailabilityFilter(request, joinManager.getScheduleJoin(), criteriaBuilder, predicates);
//        }
//    }
//
//    private boolean hasSpecialtyFilter(DoctorSearchRequest request) {
//        return request.getSpecialtyId() != null || StringUtils.hasText(request.getSpecialtyName());
//    }
//
//    private boolean hasExperienceFilter(DoctorSearchRequest request) {
//        return request.getMinExperience() != null || request.getMaxExperience() != null;
//    }
//
//    private boolean hasConsultationFeeFilter(DoctorSearchRequest request) {
//        return request.getMinConsultationFee() != null || request.getMaxConsultationFee() != null;
//    }
//
//    private void addDoctorRoleFilter(Root<User> root, CriteriaBuilder criteriaBuilder,
//                                     List<Predicate> predicates) {
//        Join<User, UserRole> userRoleJoin = root.join("userRoles", JoinType.INNER);
//        predicates.add(criteriaBuilder.and(
//                criteriaBuilder.equal(userRoleJoin.get("role").get("name"), "DOCTOR"),
//                criteriaBuilder.isTrue(userRoleJoin.get("isActive"))
//        ));
//    }
//
//    private void addDoctorNameFilter(String doctorName, Join<User, UserProfile> userProfileJoin,
//                                     CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        String searchTerm = doctorName.trim().toLowerCase();
//        List<Predicate> namePredicates = new ArrayList<>();
//
//        // Tìm theo firstName và lastName
//        namePredicates.add(createLikeExpression(userProfileJoin.get("firstName"), searchTerm, criteriaBuilder));
//        namePredicates.add(createLikeExpression(userProfileJoin.get("lastName"), searchTerm, criteriaBuilder));
//
//        // Tìm theo fullName (firstName + " " + lastName)
//        Expression<String> fullNameExpression = createFullNameExpression(userProfileJoin, criteriaBuilder);
//        namePredicates.add(criteriaBuilder.like(fullNameExpression, "%" + searchTerm + "%"));
//
//        // Tìm theo từng từ nếu có nhiều từ
//        addWordSearchPredicates(searchTerm, userProfileJoin, criteriaBuilder, namePredicates);
//
//        predicates.add(criteriaBuilder.or(namePredicates.toArray(new Predicate[0])));
//    }
//
//    private Predicate createLikeExpression(Path<String> field, String searchTerm, CriteriaBuilder criteriaBuilder) {
//        return criteriaBuilder.like(criteriaBuilder.lower(field), "%" + searchTerm + "%");
//    }
//
//    private Expression<String> createFullNameExpression(Join<User, UserProfile> userProfileJoin,
//                                                        CriteriaBuilder criteriaBuilder) {
//        return criteriaBuilder.concat(
//                criteriaBuilder.concat(
//                        criteriaBuilder.lower(userProfileJoin.get("firstName")),
//                        criteriaBuilder.literal(" ")
//                ),
//                criteriaBuilder.lower(userProfileJoin.get("lastName"))
//        );
//    }
//
//    private void addWordSearchPredicates(String searchTerm, Join<User, UserProfile> userProfileJoin,
//                                         CriteriaBuilder criteriaBuilder, List<Predicate> namePredicates) {
//        String[] searchWords = searchTerm.split("\\s+");
//        if (searchWords.length > 1) {
//            for (String word : searchWords) {
//                if (word.length() > 1) {
//                    namePredicates.add(createLikeExpression(userProfileJoin.get("firstName"), word, criteriaBuilder));
//                    namePredicates.add(createLikeExpression(userProfileJoin.get("lastName"), word, criteriaBuilder));
//                }
//            }
//        }
//    }
//
//    private void addSpecialtyFilter(DoctorSearchRequest request, Join<MedicalProfile, Specialty> specialtyJoin,
//                                    CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        List<Predicate> specialtyPredicates = new ArrayList<>();
//
//        if (request.getSpecialtyId() != null) {
//            specialtyPredicates.add(criteriaBuilder.equal(specialtyJoin.get("id"), request.getSpecialtyId()));
//        }
//
//        if (StringUtils.hasText(request.getSpecialtyName())) {
//            specialtyPredicates.add(createLikeExpression(specialtyJoin.get("name"),
//                    request.getSpecialtyName().toLowerCase(), criteriaBuilder));
//        }
//
//        if (!specialtyPredicates.isEmpty()) {
//            predicates.add(criteriaBuilder.or(specialtyPredicates.toArray(new Predicate[0])));
//        }
//    }
//
//    private void addApprovalStatusFilter(Boolean isApproved, Join<User, MedicalProfile> medicalJoin,
//                                         CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        predicates.add(criteriaBuilder.equal(medicalJoin.get("isDoctorApproved"), isApproved));
//    }
//
//    private void addExperienceFilter(DoctorSearchRequest request, Join<User, MedicalProfile> medicalJoin,
//                                     CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        addRangeFilter(medicalJoin.get("yearsOfExperience"), request.getMinExperience(),
//                request.getMaxExperience(), criteriaBuilder, predicates);
//    }
//
//    private void addConsultationFeeFilter(DoctorSearchRequest request, Join<User, MedicalProfile> medicalJoin,
//                                          CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        addRangeFilter(medicalJoin.get("consultationFee"), request.getMinConsultationFee(),
//                request.getMaxConsultationFee(), criteriaBuilder, predicates);
//    }
//
//    private <T extends Comparable<T>> void addRangeFilter(Path<T> field, T minValue, T maxValue,
//                                                          CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        if (minValue != null) {
//            predicates.add(criteriaBuilder.greaterThanOrEqualTo(field, minValue));
//        }
//        if (maxValue != null) {
//            predicates.add(criteriaBuilder.lessThanOrEqualTo(field, maxValue));
//        }
//    }
//
//    private void addQualificationFilter(String qualification, Join<User, MedicalProfile> medicalJoin,
//                                        CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        predicates.add(createLikeExpression(medicalJoin.get("qualification"),
//                qualification.toLowerCase(), criteriaBuilder));
//    }
//
//    private void addAvailabilityFilter(DoctorSearchRequest request, Join<User, DoctorSchedule> scheduleJoin,
//                                       CriteriaBuilder criteriaBuilder, List<Predicate> predicates) {
//        LocalDate availableDate = request.getAvailableDate();
//        DayOfWeek dayOfWeek = availableDate.getDayOfWeek();
//        int dayOfWeekValue = dayOfWeek.getValue();
//
//        List<Predicate> availabilityPredicates = new ArrayList<>();
//
//        // Lọc theo ngày trong tuần và trạng thái active
//        availabilityPredicates.add(criteriaBuilder.equal(scheduleJoin.get("dayOfWeek"), dayOfWeekValue));
//        availabilityPredicates.add(criteriaBuilder.isTrue(scheduleJoin.get("isActive")));
//
//        // Lọc theo thời gian nếu có
//        addTimeRangeFilter(request, scheduleJoin, criteriaBuilder, availabilityPredicates);
//
//        predicates.add(criteriaBuilder.and(availabilityPredicates.toArray(new Predicate[0])));
//    }
//
//    private void addTimeRangeFilter(DoctorSearchRequest request, Join<User, DoctorSchedule> scheduleJoin,
//                                    CriteriaBuilder criteriaBuilder, List<Predicate> availabilityPredicates) {
//        if (request.getPreferredStartTime() != null) {
//            availabilityPredicates.add(criteriaBuilder.lessThanOrEqualTo(
//                    scheduleJoin.get("startTime"), request.getPreferredStartTime()));
//        }
//
//        if (request.getPreferredEndTime() != null) {
//            availabilityPredicates.add(criteriaBuilder.greaterThanOrEqualTo(
//                    scheduleJoin.get("endTime"), request.getPreferredEndTime()));
//        }
//    }
//
//}
