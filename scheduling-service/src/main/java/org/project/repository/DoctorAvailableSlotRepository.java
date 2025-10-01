package org.project.repository;

import jakarta.persistence.LockModeType;

import org.project.model.DoctorAvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorAvailableSlotRepository extends JpaRepository <DoctorAvailableSlot, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM DoctorAvailableSlot s WHERE s.id = :slotId")
    Optional<DoctorAvailableSlot> findByIdWithLock(@Param("slotId") UUID slotId);

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

}
