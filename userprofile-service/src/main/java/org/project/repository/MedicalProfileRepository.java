package org.project.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.project.dto.response.MedicalProfileResponse;
import org.project.model.MedicalProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, UUID> {

    Optional<MedicalProfileResponse> findByUserId(UUID userId);

    boolean existsByLicenseNumber(String licenseNumber);

    @Query("""
        SELECT new org.project.dto.response.MedicalProfileResponse(
            mp.id,
            up.userId,
            up.firstName,
            up.lastName,
            mp.licenseNumber,
            up.gender,
            up.phone,
            up.avatarUrl,
            s.id,
            s.name,
            mp.qualification,
            mp.yearsOfExperience,
            mp.consultationFee,
            mp.bio,
            mp.isDoctorApproved
        )
        FROM UserProfile up
        LEFT JOIN MedicalProfile mp ON mp.userId = up.userId
        LEFT JOIN Specialty s ON s.id = mp.specialty.id
        WHERE up.userId = :userId
    """)
    Optional<MedicalProfileResponse> findMedicalProfileResponseByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT up.userId
        FROM UserProfile up
        LEFT JOIN MedicalProfile mp ON mp.userId = up.userId
        WHERE mp.isDoctorApproved = true
        ORDER BY up.createdAt DESC
    """)
    List<UUID> findApprovedDoctorIds(Pageable pageable);

    @Query("""
        SELECT mp
        FROM MedicalProfile mp
        JOIN FETCH mp.specialty s
        WHERE mp.specialty.id = :specialtyId
        AND mp.isDoctorApproved = true
    """)
    List<MedicalProfile> findApprovedMedicalProfilesBySpecialtyId(@Param("specialtyId") UUID specialtyId);

    @Query("""
        SELECT COUNT(mp)
        FROM MedicalProfile mp
        WHERE mp.specialty.id = :specialtyId
        AND mp.isDoctorApproved = true
    """)
    Long countApprovedDoctorsBySpecialtyId(@Param("specialtyId") UUID specialtyId);
}
