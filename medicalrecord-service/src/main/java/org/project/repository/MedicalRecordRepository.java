package org.project.repository;

import jakarta.persistence.LockModeType;
import org.project.model.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {
    /**
     * Tìm hồ sơ bệnh án theo ID cuộc hẹn với khóa bi quan
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mr FROM MedicalRecord mr WHERE mr.appointmentId = :appointmentId")
    Optional<MedicalRecord> findByAppointmentIdWithLock(@Param("appointmentId") UUID appointmentId);

    // Tìm hồ sơ bệnh án theo ID cuộc hẹn
    @Query("SELECT mr FROM MedicalRecord mr " +
            "WHERE mr.appointmentId = :appointmentId")
    Optional<MedicalRecord> findByAppointmentId(@Param("appointmentId") UUID appointmentId);

    // Tìm tất cả hồ sơ bệnh án của một bệnh nhân
//    @Query("SELECT mr FROM MedicalRecord mr " +
//            "JOIN FETCH mr.appointment a " +
//            "JOIN FETCH a.doctor d " +
//            "JOIN FETCH a.patient p " +
//            "WHERE p.id = :patientId " +
//            "ORDER BY mr.createdAt DESC")
//    Page<MedicalRecord> findByPatientId(@Param("patientId") UUID patientId, Pageable pageable);


    // Tìm tất cả hồ sơ bệnh án được tạo bởi một bác sĩ
//    @Query("SELECT mr FROM MedicalRecord mr " +
//            "JOIN FETCH mr.appointment a " +
//            "JOIN FETCH a.doctor d " +
//            "JOIN FETCH a.patient p " +
//            "WHERE d.id = :doctorId " +
//            "ORDER BY mr.createdAt DESC")
//    Page<MedicalRecord> findByDoctorId(@Param("doctorId") UUID doctorId, Pageable pageable);


    @Query(value = """
            SELECT 
                mr.id, 
                a.id AS appointmentId, 
                a.appointment_date AS appointmentDate,
                das.start_time AS appointmentTime, 
                a.status AS appointmentStatus,
                a.consultation_fee AS consultationFee, 
                a.reason AS appointmentReason,
                a.notes AS appointmentNotes, 
                a.doctor_notes AS doctorNotes,
            
                u.id AS doctorId, 
                CONCAT(d.first_name, ' ', d.last_name) AS doctorName,
                u.email AS doctorEmail, 
                s.name AS doctorSpecialty, 
                s.id AS doctorSpecialtyCode,
                mp.license_number AS doctorLicenseNumber, 
                mp.qualification AS doctorQualification,
                mp.years_of_experience AS doctorYearsOfExperience, 
                mp.bio AS doctorBio,
            
                u2.id AS patientId, 
                CONCAT(up2.first_name, ' ', up2.last_name) AS patientName,
                u2.email AS patientEmail, 
                up2.phone AS patientPhone, 
                up2.date_of_birth AS patientDateOfBirth,
                up2.gender AS patientGender, 
                mp2.blood_type AS patientBloodType, 
                mp2.allergies AS patientAllergies,
                mp2.medical_history AS patientMedicalHistory, 
                mp2.emergency_contact_name AS patientEmergencyContactName,
                mp2.emergency_contact_phone AS patientEmergencyContactPhone, 
            
                mr.diagnosis, 
                mr.prescription, 
                mr.test_results, 
                mr.follow_up_notes, 
                mr.created_at, 
                mr.updated_at,
            
                CONCAT(d.first_name, ' ', d.last_name) AS createdBy,
                CONCAT(d.first_name, ' ', d.last_name) AS lastUpdatedBy
            FROM medical_records mr
            LEFT JOIN appointments a ON mr.appointment_id = a.id
            LEFT JOIN users u ON u.id = a.doctor_user_id 
            LEFT JOIN user_profiles d ON d.user_id = u.id 
            LEFT JOIN medical_profiles mp ON mp.user_id = u.id 
            LEFT JOIN specialties s ON s.id = mp.specialty_id 
            LEFT JOIN doctor_available_slots das ON das.id = a.slot_id 
            LEFT JOIN users u2 ON u2.id = a.patient_user_id 
            LEFT JOIN medical_profiles mp2 ON mp2.user_id = u2.id 
            LEFT JOIN user_profiles up2 ON up2.user_id = u2.id 
            WHERE a.id = :appointmentId
            """, nativeQuery = true)
    Optional<MedicalRecordProjection> findProjectionByAppointmentId(@Param("appointmentId") UUID appointmentId);

    @Query(value = """
            SELECT 
                mr.id, 
                a.id AS appointmentId, 
                a.appointment_date AS appointmentDate,
                das.start_time AS appointmentTime, 
                a.status AS appointmentStatus,
                a.consultation_fee AS consultationFee, 
                a.reason AS appointmentReason,
                a.notes AS appointmentNotes, 
                a.doctor_notes AS doctorNotes,
            
                u.id AS doctorId, 
                CONCAT(d.first_name, ' ', d.last_name) AS doctorName,
                u.email AS doctorEmail, 
                s.name AS doctorSpecialty, 
                s.id AS doctorSpecialtyCode,
                mp.license_number AS doctorLicenseNumber, 
                mp.qualification AS doctorQualification,
                mp.years_of_experience AS doctorYearsOfExperience, 
                mp.bio AS doctorBio,
            
                u2.id AS patientId, 
                CONCAT(up2.first_name, ' ', up2.last_name) AS patientName,
                u2.email AS patientEmail, 
                up2.phone AS patientPhone, 
                up2.date_of_birth AS patientDateOfBirth,
                up2.gender AS patientGender, 
                mp2.blood_type AS patientBloodType, 
                mp2.allergies AS patientAllergies,
                mp2.medical_history AS patientMedicalHistory, 
                mp2.emergency_contact_name AS patientEmergencyContactName,
                mp2.emergency_contact_phone AS patientEmergencyContactPhone, 
            
                mr.diagnosis, 
                mr.prescription, 
                mr.test_results, 
                mr.follow_up_notes, 
                mr.created_at, 
                mr.updated_at,
            
                CONCAT(d.first_name, ' ', d.last_name) AS createdBy,
                CONCAT(d.first_name, ' ', d.last_name) AS lastUpdatedBy
            FROM medical_records mr
            LEFT JOIN appointments a ON mr.appointment_id = a.id
            LEFT JOIN users u ON u.id = a.doctor_user_id 
            LEFT JOIN user_profiles d ON d.user_id = u.id 
            LEFT JOIN medical_profiles mp ON mp.user_id = u.id 
            LEFT JOIN specialties s ON s.id = mp.specialty_id 
            LEFT JOIN doctor_available_slots das ON das.id = a.slot_id 
            LEFT JOIN users u2 ON u2.id = a.patient_user_id 
            LEFT JOIN medical_profiles mp2 ON mp2.user_id = u2.id 
            LEFT JOIN user_profiles up2 ON up2.user_id = u2.id 
            WHERE mr.id = :medicalRecordId
            """, nativeQuery = true)
    Optional<MedicalRecordProjection> findProjectionByMedicalRecordId(@Param("medicalRecordId") UUID medicalRecordId);

    @Query("SELECT COUNT(mr) > 0 FROM MedicalRecord mr WHERE mr.appointmentId = :appointmentId")
    boolean existsByAppointmentId(@Param("appointmentId") UUID appointmentId);


    /**
     * Lấy danh sách tóm tắt hồ sơ bệnh án theo ID bác sĩ có phân trang
     */
//    @Query("""
//            SELECT new org.project.dto.response.MedicalRecordSummaryResponse(
//                mr.id,
//                a.id,
//                a.appointmentDate,
//                s.startTime,
//                a.status,
//                a.consultationFee,
//                d.id,
//                CONCAT(du.firstName, ' ', du.lastName),
//                ds.name,
//                dm.licenseNumber,
//                p.id,
//                CONCAT(pu.firstName, ' ', pu.lastName),
//                YEAR(CURRENT_DATE) - YEAR(pu.dateOfBirth),
//                pu.gender,
//                CASE WHEN LENGTH(mr.diagnosis) > 100 THEN CONCAT(SUBSTRING(mr.diagnosis, 1, 100), '...') ELSE mr.diagnosis END,
//                CASE WHEN LENGTH(mr.prescription) > 100 THEN CONCAT(SUBSTRING(mr.prescription, 1, 100), '...') ELSE mr.prescription END,
//                CASE WHEN mr.testResults IS NOT NULL AND LENGTH(mr.testResults) > 0 THEN true ELSE false END,
//                CASE WHEN mr.followUpNotes IS NOT NULL AND LENGTH(mr.followUpNotes) > 0 THEN true ELSE false END,
//                mr.createdAt,
//                mr.updatedAt,
//                CONCAT(du.firstName, ' ', du.lastName),
//                true,
//                true,
//                CASE WHEN mr.createdAt >= :sevenDaysAgo THEN true ELSE false END
//            )
//            FROM MedicalRecord mr
//            JOIN mr.appointment a
//            JOIN a.doctor d
//            JOIN d.userProfile du
//            JOIN d.medicalProfile dm
//            JOIN dm.specialty ds
//            JOIN a.slot s
//            JOIN a.patient p
//            JOIN p.userProfile pu
//            WHERE d.id = :doctorId
//            ORDER BY mr.createdAt DESC
//            """)
//    Page<MedicalRecordSummaryResponse> findSummariesByDoctorId(@Param("doctorId") UUID doctorId, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo, Pageable pageable);


    /**
     * Lấy danh sách tóm tắt hồ sơ bệnh án theo ID bệnh nhân có phân trang
     */
//    @Query("""
//            SELECT new org.project.appointment_project.medicalrecord.dto.response.MedicalRecordSummaryResponse(
//                mr.id,
//                a.id,
//                a.appointmentDate,
//                s.startTime,
//                a.status,
//                a.consultationFee,
//                d.id,
//                CONCAT(du.firstName, ' ', du.lastName),
//                ds.name,
//                dm.licenseNumber,
//                p.id,
//                CONCAT(pu.firstName, ' ', pu.lastName),
//                YEAR(CURRENT_DATE) - YEAR(pu.dateOfBirth),
//                pu.gender,
//                CASE WHEN LENGTH(mr.diagnosis) > 100 THEN CONCAT(SUBSTRING(mr.diagnosis, 1, 100), '...') ELSE mr.diagnosis END,
//                CASE WHEN LENGTH(mr.prescription) > 100 THEN CONCAT(SUBSTRING(mr.prescription, 1, 100), '...') ELSE mr.prescription END,
//                CASE WHEN mr.testResults IS NOT NULL AND LENGTH(mr.testResults) > 0 THEN true ELSE false END,
//                CASE WHEN mr.followUpNotes IS NOT NULL AND LENGTH(mr.followUpNotes) > 0 THEN true ELSE false END,
//                mr.createdAt,
//                mr.updatedAt,
//                CONCAT(du.firstName, ' ', du.lastName),
//                true,
//                true,
//                CASE WHEN mr.createdAt >= :sevenDaysAgo THEN true ELSE false END
//            )
//            FROM MedicalRecord mr
//            JOIN mr.appointment a
//            JOIN a.doctor d
//            JOIN d.userProfile du
//            JOIN d.medicalProfile dm
//            JOIN dm.specialty ds
//            JOIN a.slot s
//            JOIN a.patient p
//            JOIN p.userProfile pu
//            WHERE p.id = :patientId
//            ORDER BY mr.createdAt DESC
//            """)
//    Page<MedicalRecordSummaryResponse> findSummariesByPatientId(@Param("patientId") UUID patientId, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,Pageable pageable);

    /**
     * Lấy tất cả tóm tắt hồ sơ bệnh án với các bộ lọc tùy chọn
     */
//    @Query("""
//            SELECT new org.project.appointment_project.medicalrecord.dto.response.MedicalRecordSummaryResponse(
//               mr.id,
//                a.id,
//                a.appointmentDate,
//                s.startTime,
//                a.status,
//                a.consultationFee,
//                d.id,
//                CONCAT(du.firstName, ' ', du.lastName),
//                ds.name,
//                dm.licenseNumber,
//                p.id,
//                CONCAT(pu.firstName, ' ', pu.lastName),
//                YEAR(CURRENT_DATE) - YEAR(pu.dateOfBirth),
//                pu.gender,
//                CASE WHEN LENGTH(mr.diagnosis) > 100 THEN CONCAT(SUBSTRING(mr.diagnosis, 1, 100), '...') ELSE mr.diagnosis END,
//                CASE WHEN LENGTH(mr.prescription) > 100 THEN CONCAT(SUBSTRING(mr.prescription, 1, 100), '...') ELSE mr.prescription END,
//                CASE WHEN mr.testResults IS NOT NULL AND LENGTH(mr.testResults) > 0 THEN true ELSE false END,
//                CASE WHEN mr.followUpNotes IS NOT NULL AND LENGTH(mr.followUpNotes) > 0 THEN true ELSE false END,
//                mr.createdAt,
//                mr.updatedAt,
//                CONCAT(du.firstName, ' ', du.lastName),
//                true,
//                true,
//                CASE WHEN mr.createdAt >= :sevenDaysAgo THEN true ELSE false END
//            )
//            FROM MedicalRecord mr
//            JOIN mr.appointment a
//            JOIN a.doctor d
//            JOIN d.userProfile du
//            JOIN d.medicalProfile dm
//            JOIN dm.specialty ds
//            JOIN a.slot s
//            JOIN a.patient p
//            JOIN p.userProfile pu
//            WHERE (:doctorId IS NULL OR d.id = :doctorId)
//            AND (:patientId IS NULL OR p.id = :patientId)
//            AND (:specialtyId IS NULL OR ds.id = :specialtyId)
//            AND (:fromDate IS NULL OR a.appointmentDate >= :fromDate)
//            AND (:toDate IS NULL OR a.appointmentDate <= :toDate)
//            AND (:status IS NULL OR a.status = :status)
//            ORDER BY mr.createdAt DESC
//            """)
//    Page<MedicalRecordSummaryResponse> findSummariesWithFilters(
//            @Param("doctorId") UUID doctorId,
//            @Param("patientId") UUID patientId,
//            @Param("specialtyId") UUID specialtyId,
//            @Param("fromDate") LocalDate fromDate,
//            @Param("toDate") LocalDate toDate,
//            @Param("status") Status status,
//            @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
//            Pageable pageable
//    );


    /**
     * Tìm kiếm hồ sơ bệnh án theo tên bệnh nhân, tên bác sĩ, hoặc chẩn đoán
     */
//    @Query("""
//                SELECT new org.project.appointment_project.medicalrecord.dto.response.MedicalRecordSummaryResponse(
//                    mr.id,
//                    a.id,
//                    a.appointmentDate,
//                    s.startTime,
//                    a.status,
//                    a.consultationFee,
//                    d.id,
//                    CONCAT(du.firstName, ' ', du.lastName),
//                    ds.name,
//                    dm.licenseNumber,
//                    p.id,
//                    CONCAT(pu.firstName, ' ', pu.lastName),
//                    YEAR(CURRENT_DATE) - YEAR(pu.dateOfBirth),
//                    pu.gender,
//                    CASE WHEN LENGTH(mr.diagnosis) > 100 THEN CONCAT(SUBSTRING(mr.diagnosis, 1, 100), '...') ELSE mr.diagnosis END,
//                    CASE WHEN LENGTH(mr.prescription) > 100 THEN CONCAT(SUBSTRING(mr.prescription, 1, 100), '...') ELSE mr.prescription END,
//                    CASE WHEN mr.testResults IS NOT NULL AND LENGTH(mr.testResults) > 0 THEN true ELSE false END,
//                    CASE WHEN mr.followUpNotes IS NOT NULL AND LENGTH(mr.followUpNotes) > 0 THEN true ELSE false END,
//                    mr.createdAt,
//                    mr.updatedAt,
//                    CONCAT(du.firstName, ' ', du.lastName),
//                    true,
//                    true,
//                    CASE WHEN mr.createdAt >= :sevenDaysAgo THEN true ELSE false END
//                )
//                FROM MedicalRecord mr
//                JOIN mr.appointment a
//                JOIN a.doctor d
//                JOIN d.userProfile du
//                JOIN d.medicalProfile dm
//                JOIN dm.specialty ds
//                LEFT JOIN a.slot s
//                JOIN a.patient p
//                JOIN p.userProfile pu
//                WHERE (
//                    LOWER(CONCAT(pu.firstName, ' ', pu.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
//                    OR LOWER(CONCAT(du.firstName, ' ', du.lastName)) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
//                    OR LOWER(mr.diagnosis) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
//                    OR LOWER(ds.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
//                )
//                ORDER BY mr.createdAt DESC
//            """)
//    Page<MedicalRecordSummaryResponse> searchMedicalRecordSummaries(@Param("searchTerm") String searchTerm, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo, Pageable pageable);


    /**
     * Lấy các hồ sơ bệnh án gần đây (trong N ngày gần đây)
     */
//    @Query("""
//            SELECT new org.project.appointment_project.medicalrecord.dto.response.MedicalRecordSummaryResponse(
//               mr.id,
//                a.id,
//                a.appointmentDate,
//                s.startTime,
//                a.status,
//                a.consultationFee,
//                d.id,
//                CONCAT(du.firstName, ' ', du.lastName),
//                ds.name,
//                dm.licenseNumber,
//                p.id,
//                CONCAT(pu.firstName, ' ', pu.lastName),
//                YEAR(CURRENT_DATE) - YEAR(pu.dateOfBirth),
//                pu.gender,
//                CASE WHEN LENGTH(mr.diagnosis) > 100 THEN CONCAT(SUBSTRING(mr.diagnosis, 1, 100), '...') ELSE mr.diagnosis END,
//                CASE WHEN LENGTH(mr.prescription) > 100 THEN CONCAT(SUBSTRING(mr.prescription, 1, 100), '...') ELSE mr.prescription END,
//                CASE WHEN mr.testResults IS NOT NULL AND LENGTH(mr.testResults) > 0 THEN true ELSE false END,
//                CASE WHEN mr.followUpNotes IS NOT NULL AND LENGTH(mr.followUpNotes) > 0 THEN true ELSE false END,
//                mr.createdAt,
//                mr.updatedAt,
//                CONCAT(du.firstName, ' ', du.lastName),
//                true,
//                true,
//                true
//            )
//            FROM MedicalRecord mr
//            JOIN mr.appointment a
//            JOIN a.doctor d
//            JOIN d.userProfile du
//            JOIN d.medicalProfile dm
//            JOIN dm.specialty ds
//            JOIN a.slot s
//            JOIN a.patient p
//            JOIN p.userProfile pu
//            WHERE mr.createdAt >= :cutoffDate
//            ORDER BY mr.createdAt DESC
//            """)
//    Page<MedicalRecordSummaryResponse> findRecentMedicalRecordSummaries(@Param("cutoffDate") LocalDateTime cutoffDate, Pageable pageable);

    /**
     * Lấy hồ sơ bệnh án theo chuyên khoa
     */
//    @Query("""
//            SELECT new org.project.appointment_project.medicalrecord.dto.response.MedicalRecordSummaryResponse(
//               mr.id,
//                a.id,
//                a.appointmentDate,
//                s.startTime,
//                a.status,
//                a.consultationFee,
//                d.id,
//                CONCAT(du.firstName, ' ', du.lastName),
//                ds.name,
//                dm.licenseNumber,
//                p.id,
//                CONCAT(pu.firstName, ' ', pu.lastName),
//                YEAR(CURRENT_DATE) - YEAR(pu.dateOfBirth),
//                pu.gender,
//                CASE WHEN LENGTH(mr.diagnosis) > 100 THEN CONCAT(SUBSTRING(mr.diagnosis, 1, 100), '...') ELSE mr.diagnosis END,
//                CASE WHEN LENGTH(mr.prescription) > 100 THEN CONCAT(SUBSTRING(mr.prescription, 1, 100), '...') ELSE mr.prescription END,
//                CASE WHEN mr.testResults IS NOT NULL AND LENGTH(mr.testResults) > 0 THEN true ELSE false END,
//                CASE WHEN mr.followUpNotes IS NOT NULL AND LENGTH(mr.followUpNotes) > 0 THEN true ELSE false END,
//                mr.createdAt,
//                mr.updatedAt,
//                CONCAT(du.firstName, ' ', du.lastName),
//                true,
//                true,
//                CASE WHEN mr.createdAt >= :sevenDaysAgo THEN true ELSE false END
//            )
//            FROM MedicalRecord mr
//            JOIN mr.appointment a
//            JOIN a.doctor d
//            JOIN d.userProfile du
//            JOIN d.medicalProfile dm
//            JOIN dm.specialty ds
//            JOIN a.slot s
//            JOIN a.patient p
//            JOIN p.userProfile pu
//            WHERE ds.id = :specialtyId
//            ORDER BY mr.createdAt DESC
//            """)
//    Page<MedicalRecordSummaryResponse> findSummariesBySpecialtyId(@Param("specialtyId") UUID specialtyId, @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo, Pageable pageable);

}
