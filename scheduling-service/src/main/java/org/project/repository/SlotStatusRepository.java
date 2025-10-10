package org.project.repository;

import org.project.model.DoctorAvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SlotStatusRepository extends JpaRepository<DoctorAvailableSlot, UUID > {

     //Tìm slot theo ID với thông tin doctor
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.id = :slotId")
    Optional<DoctorAvailableSlot> findByIdWithDoctor(@Param("slotId") UUID slotId);

     //Tìm các slot đang available của doctor trong khoảng thời gian
    @Query("SELECT s FROM DoctorAvailableSlot s " +
            "WHERE s.doctorId = :doctorId " +
            "AND s.slotDate = :slotDate " +
            "AND s.startTime >= :startTime " +
            "AND s.endTime <= :endTime " +
            "AND s.isAvailable = true")
    List<DoctorAvailableSlot> findAvailableSlotsByDoctorAndTimeRange(
            @Param("doctorId") UUID doctorId,
            @Param("slotDate") LocalDate slotDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
