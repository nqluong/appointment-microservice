package org.project.repository;

import org.project.model.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, UUID> {
    boolean existsByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM InvalidatedToken it WHERE it.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(it) > 0 FROM InvalidatedToken it WHERE it.tokenHash = :tokenHash AND it.expiresAt > :now")
    boolean isTokenInvalid(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);
}
