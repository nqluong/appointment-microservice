package org.project.repository;

import org.project.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    @Query("SELECT p FROM PasswordResetToken p WHERE p.token = :token AND (p.isUsed = false OR p.isUsed IS NULL)")
    Optional<PasswordResetToken> findByTokenAndUseFalse(String token);

    @Query("SELECT p FROM PasswordResetToken p WHERE p.user.email = :email AND (p.isUsed = false OR p.isUsed IS NULL) ORDER BY p.createdAt DESC")
    Optional<PasswordResetToken> findLatestByUserEmail(String email);

    @Modifying
    @Query("UPDATE PasswordResetToken p SET p.isUsed = true WHERE p.user.id = :userId AND (p.isUsed = false OR p.isUsed IS NULL)")
    void markAllTokensAsUsedByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteExpiredTokens(LocalDateTime now);
}
