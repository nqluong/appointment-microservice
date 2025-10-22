package org.project.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.project.model.DoctorAvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

@Repository
public interface DoctorAvailableSlotRepository extends JpaRepository <DoctorAvailableSlot, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DoctorAvailableSlot s WHERE s.id = :slotId")
    Optional<DoctorAvailableSlot> findByIdWithLock(@Param("slotId") UUID slotId);

    @Query("SELECT DISTINCT d.doctorId FROM DoctorAvailableSlot d " +
            "WHERE d.slotDate BETWEEN :startDate AND :endDate " +
            "AND (:isAvailable IS NULL OR d.isAvailable = :isAvailable)")
    List<UUID> findDistinctDoctorIdsByDateRange(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isAvailable") Boolean isAvailable
    );

    @Query("SELECT DISTINCT d.doctorId FROM DoctorAvailableSlot d")
    List<UUID> findAllDistinctDoctorIds();

    @Query("SELECT COUNT(d) FROM DoctorAvailableSlot d " +
            "WHERE d.doctorId = :doctorId " +
            "AND d.slotDate BETWEEN :startDate AND :endDate " +
            "AND d.isAvailable = true")
    long countAvailableSlotsByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT d.doctorId, COUNT(d) as slotCount " +
            "FROM DoctorAvailableSlot d " +
            "WHERE d.slotDate BETWEEN :startDate AND :endDate " +
            "AND d.isAvailable = true " +
            "GROUP BY d.doctorId " +
            "HAVING COUNT(d) >= :minSlots " +
            "ORDER BY slotCount DESC")
    List<Object[]> findDoctorIdsWithMinimumSlots(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minSlots") long minSlots
    );

    /**
     * Tìm tất cả slots của bác sĩ trong một ngày cụ thể
     */
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.doctorId = :doctorUserId " +
            "AND s.slotDate = :slotDate " +
            "ORDER BY s.startTime")
    List<DoctorAvailableSlot> findByDoctorUserIdAndSlotDate(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("slotDate") LocalDate slotDate
    );

    /**
     * Tìm slots của bác sĩ trong khoảng thời gian cụ thể
     */
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.doctorId= :doctorUserId " +
            "AND s.slotDate = :slotDate " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime " +
            "ORDER BY s.startTime")
    List<DoctorAvailableSlot> findByDoctorUserIdAndSlotDateAndTimeRange(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("slotDate") LocalDate slotDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Tìm slots của bác sĩ từ thời điểm startTime đến cuối ngày
     */
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.doctorId = :doctorUserId " +
            "AND s.slotDate = :slotDate " +
            "AND s.startTime >= :startTime " +
            "ORDER BY s.startTime")
    List<DoctorAvailableSlot> findByDoctorUserIdAndSlotDateAndStartTimeAfter(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("slotDate") LocalDate slotDate,
            @Param("startTime") LocalTime startTime
    );

    /**
     * Tìm slots của bác sĩ từ đầu ngày đến thời điểm endTime
     */
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.doctorId = :doctorUserId " +
            "AND s.slotDate = :slotDate " +
            "AND s.endTime <= :endTime " +
            "ORDER BY s.startTime")
    List<DoctorAvailableSlot> findByDoctorUserIdAndSlotDateAndEndTimeBefore(
            @Param("doctorUserId") UUID doctorUserId,
            @Param("slotDate") LocalDate slotDate,
            @Param("endTime") LocalTime endTime
    );

    /**
     * Tìm slots của bác sĩ trong khoảng ngày (cho Cache Service)
     */
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.doctorId = :doctorId " +
            "AND s.slotDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.slotDate, s.startTime")
    List<DoctorAvailableSlot> findSlotsByDoctorAndDateRange(
            @Param("doctorId") UUID doctorId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
