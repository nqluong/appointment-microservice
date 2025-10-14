package org.project.repository;

import org.project.model.SlotReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlotReservationRepository extends JpaRepository<SlotReservation, UUID> {


    Optional<SlotReservation> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT sr FROM SlotReservation sr " +
            "WHERE sr.slotId = :slotId " +
            "AND sr.active = true " +
            "AND sr.expiresAt > :now")
    Optional<SlotReservation> findActiveReservationBySlotId(
            @Param("slotId") UUID slotId,
            @Param("now") LocalDateTime now
    );

    /**
     * Check xem slot có reservation active không
     */
    @Query("SELECT CASE WHEN COUNT(sr) > 0 THEN true ELSE false END " +
            "FROM SlotReservation sr " +
            "WHERE sr.slotId = :slotId " +
            "AND sr.active = true " +
            "AND sr.expiresAt > CURRENT_TIMESTAMP")
    boolean existsActiveReservation(@Param("slotId") UUID slotId);


    @Query("SELECT sr FROM SlotReservation sr " +
            "WHERE sr.active = true " +
            "AND sr.confirmed = false " +
            "AND sr.expiresAt < :now")
    List<SlotReservation> findExpiredReservations(@Param("now") LocalDateTime now);


    @Query("SELECT sr FROM SlotReservation sr " +
            "WHERE sr.patientId = :patientId " +
            "AND sr.active = true " +
            "AND sr.expiresAt > CURRENT_TIMESTAMP")
    List<SlotReservation> findActiveReservationsByPatient(@Param("patientId") UUID patientId);

    List<SlotReservation> findBySlotIdOrderByCreatedAtDesc(UUID slotId);
}
