package org.project.repository;

import org.project.model.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {
    List<DoctorSchedule> findByDoctorIdAndIsActiveTrue(UUID doctorId);

    List<DoctorSchedule> findByDoctorId(UUID doctorId);

    boolean existsByDoctorId(UUID doctorId);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctorUserId = :doctorId AND ds.isActive = true ORDER BY ds.dayOfWeek")
    List<DoctorSchedule> findActiveDoctorSchedulesByDoctorId(@Param("doctorId") UUID doctorId);

    @Modifying
    @Query("UPDATE DoctorSchedule ds SET ds.isActive = false WHERE ds.doctorUserId = :doctorId")
    int deactivateSchedulesByDoctorId(@Param("doctorId") UUID doctorId);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.dayOfWeek = :dayOfWeek AND ds.isActive = true")
    List<DoctorSchedule> findByDayOfWeekAndIsActiveTrue(@Param("dayOfWeek") Integer dayOfWeek);

    @Query("SELECT DISTINCT ds.doctorUserId FROM DoctorSchedule ds WHERE ds.dayOfWeek = :dayOfWeek AND ds.isActive = true")
    List<UUID> findDoctorIdsByDayOfWeek(@Param("dayOfWeek") Integer dayOfWeek);
}
