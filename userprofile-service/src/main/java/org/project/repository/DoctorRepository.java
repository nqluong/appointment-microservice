package org.project.repository;


import java.util.List;
import java.util.UUID;

import org.project.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<UserProfile, UUID> {

    @Query(value = "SELECT " +
            "up.user_id as userId, " +
            "CONCAT(up.first_name, ' ', up.last_name) as fullName, " +
            "CAST(up.gender AS VARCHAR) as gender, " +
            "up.phone as phone, " +
            "up.avatar_url as avatarUrl, " +
            "mp.qualification as qualification, " +
            "mp.years_of_experience as yearsOfExperience, " +
            "mp.consultation_fee as consultationFee, " +
            "s.name as specialtyName, up.created_at as createdAt " +
            "FROM user_profiles up " +
            "LEFT JOIN medical_profiles mp ON mp.user_id = up.user_id " +
            "LEFT JOIN specialties s ON s.id = mp.specialty_id " +
            "WHERE up.user_id IN :userIds " +
            "AND mp.is_doctor_approved = true",
            countQuery = "SELECT COUNT(*) " +
                    "FROM user_profiles up " +
                    "LEFT JOIN medical_profiles mp ON mp.user_id = up.user_id " +
                    "WHERE up.user_id IN :userIds " +
                    "AND mp.is_doctor_approved = true",
            nativeQuery = true)
    Page<DoctorProjection> findApprovedDoctorsByUserIds(
            @Param("userIds") List<UUID> userIds,
            Pageable pageable);

    @Query(value = "SELECT " +
            "up.user_id as userId, " +
            "CONCAT(up.first_name, ' ', up.last_name) as fullName, " +
            "CAST(up.gender AS VARCHAR) as gender, " +
            "up.phone as phone, " +
            "up.avatar_url as avatarUrl, " +
            "mp.qualification as qualification, " +
            "mp.years_of_experience as yearsOfExperience, " +
            "mp.consultation_fee as consultationFee, " +
            "s.name as specialtyName, up.created_at as createdAt " +
            "FROM user_profiles up " +
            "LEFT JOIN medical_profiles mp ON mp.user_id = up.user_id " +
            "LEFT JOIN specialties s ON s.id = mp.specialty_id " +
            "WHERE up.user_id IN :userIds " +
            "AND mp.is_doctor_approved = true " +
            "AND (COALESCE(:specialtyName, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :specialtyName, '%')))",
            countQuery = "SELECT COUNT(*) " +
                    "FROM user_profiles up " +
                    "LEFT JOIN medical_profiles mp ON mp.user_id = up.user_id " +
                    "WHERE up.user_id IN :userIds " +
                    "AND mp.is_doctor_approved = true",
            nativeQuery = true)
    Page<DoctorProjection> findDoctorsWithFilters(
            @Param("userIds") List<UUID> userIds,
            @Param("specialtyName") String specialtyName,
            Pageable pageable);

    @Query(value = """ 
            SELECT 
            up.user_id as userId,
            CONCAT(up.first_name, ' ', up.last_name) as fullName, 
            CAST(up.gender AS VARCHAR) as gender, 
            up.phone as phone, 
            up.avatar_url as avatarUrl, 
            mp.qualification as qualification, 
            mp.years_of_experience as yearsOfExperience, 
            mp.consultation_fee as consultationFee, 
            s.name as specialtyName, up.created_at as createdAt 
            FROM user_profiles up 
            LEFT JOIN medical_profiles mp ON mp.user_id = up.user_id 
            LEFT JOIN specialties s ON s.id = mp.specialty_id 
            WHERE up.user_id IN :userIds 
            AND mp.is_doctor_approved = true 
            AND mp.specialty_id = :specialtyId 
            """,
            countQuery = "SELECT COUNT(*) " +
                    "FROM user_profiles up " +
                    "LEFT JOIN medical_profiles mp ON mp.user_id = up.user_id " +
                    "WHERE up.user_id IN :userIds " +
                    "AND mp.is_doctor_approved = true " +
                    "AND mp.specialty_id = :specialtyId",
            nativeQuery = true)
    Page<DoctorProjection> findDoctorsBySpecialtyId(
            @Param("userIds") List<UUID> userIds,
            @Param("specialtyId") UUID specialtyId,
            Pageable pageable);

}
