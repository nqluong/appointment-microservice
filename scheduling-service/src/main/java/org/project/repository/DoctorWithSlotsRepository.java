//package org.project.repository;
//
//import org.project.appointment_project.user.model.User;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public interface DoctorWithSlotsRepository extends JpaRepository<User, UUID> {
//
//    @Query(value = """
//    SELECT DISTINCT
//        u.id as userId,
//        up.first_name as firstName,
//        up.last_name as lastName,
//        up.phone as phone,
//        u.email as email,
//        up.date_of_birth as dateOfBirth,
//        up.avatar_url as avatarUrl,
//        up.gender as gender,
//        s.name as specialtyName,
//        mp.license_number as licenseNumber,
//        mp.qualification as qualification,
//        mp.years_of_experience as yearsOfExperience,
//        mp.consultation_fee as consultationFee
//    FROM users u
//    INNER JOIN user_profiles up ON u.id = up.user_id
//    INNER JOIN user_roles ur ON u.id = ur.user_id AND ur.is_active = true
//    INNER JOIN roles r ON ur.role_id = r.id AND r.name = 'DOCTOR'
//    INNER JOIN medical_profiles mp ON u.id = mp.user_id AND mp.is_doctor_approved = true
//    LEFT JOIN specialties s ON mp.specialty_id = s.id AND s.is_active = true
//    WHERE u.is_active = true
//        AND u.is_email_verified = true
//        AND EXISTS (
//            SELECT 1 FROM doctor_available_slots das
//            WHERE das.doctor_user_id = u.id
//                AND das.is_available = true
//                AND das.slot_date BETWEEN :startDate AND :endDate
//        )
//    ORDER BY up.first_name, up.last_name
//    """, nativeQuery = true)
//    Page<DoctorWithSlotsProjection> findDoctorsWithAvailableSlots(
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate,
//            Pageable pageable
//    );
//
//    @Query(value = """
//    SELECT
//        das.id as slotId,
//        das.slot_date as slotDate,
//        das.start_time as startTime,
//        das.end_time as endTime,
//        das.is_available as isAvailable
//    FROM doctor_available_slots das
//    WHERE das.doctor_user_id = :doctorId
//        AND das.is_available = true
//        AND das.slot_date BETWEEN :startDate AND :endDate
//    ORDER BY das.slot_date, das.start_time
//    LIMIT 3
//    """, nativeQuery = true)
//    List<SlotProjection> findAvailableSlotsByDoctorId(
//            @Param("doctorId") UUID doctorId,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate
//    );
//
//    @Query(value = """
//    SELECT
//        das.id as slotId,
//        das.slot_date as slotDate,
//        das.start_time as startTime,
//        das.end_time as endTime,
//        das.is_available as isAvailable
//    FROM doctor_available_slots das
//    WHERE das.doctor_user_id = :doctorId
//        AND das.slot_date BETWEEN :startDate AND :endDate
//    ORDER BY das.slot_date, das.start_time ASC
//    """, nativeQuery = true)
//    List<SlotProjection> findAllAvailableSlotsByDoctorId(
//            @Param("doctorId") UUID doctorId,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate
//    );
//
//    @Query(value = """
//    SELECT DISTINCT
//        u.id as userId,
//        up.first_name as firstName,
//        up.last_name as lastName,
//        up.phone as phone,
//        u.email as email,
//        up.date_of_birth as dateOfBirth,
//        up.avatar_url as avatarUrl,
//        up.gender as gender,
//        s.name as specialtyName,
//        mp.license_number as licenseNumber,
//        mp.qualification as qualification,
//        mp.years_of_experience as yearsOfExperience,
//        mp.consultation_fee as consultationFee
//    FROM users u
//    INNER JOIN user_profiles up ON u.id = up.user_id
//    INNER JOIN user_roles ur ON u.id = ur.user_id AND ur.is_active = true
//    INNER JOIN roles r ON ur.role_id = r.id AND r.name = 'DOCTOR'
//    INNER JOIN medical_profiles mp ON u.id = mp.user_id AND mp.is_doctor_approved = true
//    INNER JOIN specialties s ON mp.specialty_id = s.id AND s.is_active = true AND s.id = :specialtyId
//    WHERE u.is_active = true
//        AND u.is_email_verified = true
//        AND EXISTS (
//            SELECT 1 FROM doctor_available_slots das
//            WHERE das.doctor_user_id = u.id
//                AND das.is_available = true
//                AND das.slot_date BETWEEN :startDate AND :endDate
//        )
//    ORDER BY up.first_name, up.last_name
//    """, nativeQuery = true)
//    Page<DoctorWithSlotsProjection> findDoctorsWithAvailableSlotsBySpecialty(
//            @Param("specialtyId") UUID specialtyId,
//            @Param("startDate") LocalDate startDate,
//            @Param("endDate") LocalDate endDate,
//            Pageable pageable
//    );
//
//
//    @Query(value = """
//    SELECT DISTINCT
//        u.id as userId,
//        up.first_name as firstName,
//        up.last_name as lastName,
//        up.phone as phone,
//        u.email as email,
//        up.date_of_birth as dateOfBirth,
//        up.avatar_url as avatarUrl,
//        up.gender as gender,
//        s.name as specialtyName,
//        mp.license_number as licenseNumber,
//        mp.qualification as qualification,
//        mp.years_of_experience as yearsOfExperience,
//        mp.consultation_fee as consultationFee
//    FROM users u
//    INNER JOIN user_profiles up ON u.id = up.user_id
//    INNER JOIN user_roles ur ON u.id = ur.user_id AND ur.is_active = true
//    INNER JOIN roles r ON ur.role_id = r.id AND r.name = 'DOCTOR'
//    INNER JOIN medical_profiles mp ON u.id = mp.user_id AND mp.is_doctor_approved = true
//    LEFT JOIN specialties s ON mp.specialty_id = s.id AND s.is_active = true
//    WHERE u.is_active = true
//        AND u.is_email_verified = true
//        AND u.id = :doctorId
//    """, nativeQuery = true)
//    Optional<DoctorWithSlotsProjection> findDoctorById(@Param("doctorId") UUID doctorId);
//}
