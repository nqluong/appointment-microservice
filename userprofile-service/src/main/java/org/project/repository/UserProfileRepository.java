package org.project.repository;

import org.project.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {
    Optional<UserProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("UPDATE UserProfile u SET u.avatarUrl = :avatarUrl, u.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE u.userId = :userId")
    int updateAvatarUrl(@Param("userId") UUID userId, @Param("avatarUrl") String avatarUrl);
}
