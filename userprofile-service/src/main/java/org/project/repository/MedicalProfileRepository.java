package org.project.repository;

import org.project.dto.response.MedicalProfileResponse;
import org.project.model.MedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalProfileRepository extends JpaRepository<MedicalProfile, UUID> {
    boolean existsByLicenseNumber(String licenseNumber);

    @Query("""
        SELECT new org.project.dto.response.MedicalProfileResponse(
            mp.id,
            up.userId,
            up.firstName,
            up.lastName,
            mp.licenseNumber,
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
    Optional<MedicalProfileResponse> findByUserId(@Param("userId") UUID userId);
}
