package org.project.repository;

import jakarta.persistence.LockModeType;
import org.project.enums.Status;
import org.project.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {


     //Kiểm tra xem bệnh nhân có lịch hẹn trùng thời gian không
     @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.patientUserId = :patientId
            AND a.appointmentDate = :appointmentDate
            AND a.status IN ('PENDING', 'CONFIRMED')
            AND (
                (a.startTime < :endTime AND a.endTime > :startTime)
            )
        """)
    boolean existsOverlappingAppointment(
            @Param("patientId") UUID patientId,
            @Param("appointmentDate") LocalDate appointmentDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

     //Đếm số lượng lịch hẹn đang pending của bệnh nhân
    @Query("""
        SELECT COUNT(a) FROM Appointment a
        WHERE a.patientUserId = :patientId
        AND a.status = 'PENDING'
        """)
    long countPendingAppointmentsByPatient(@Param("patientId") UUID patientId);

    //Tìm tất cả lịch hẹn của bệnh nhân theo trạng thái
    @Query("""
        SELECT a FROM Appointment a
        WHERE (a.patientUserId = :userId or a.doctorUserId = :userId)
        AND a.status IN :statuses
    """)
    Page<Appointment> findByUserIdAndStatusIn(UUID userId, List<Status> statuses, Pageable pageable);

    //Tìm tất cả lịch hẹn của bác sĩ theo trạng thái
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.doctorUserId = :doctorId
        AND a.status IN :statuses
    """)
    Page<Appointment> findByDoctorIdAndStatusIn(UUID doctorId, List<Status> statuses, Pageable pageable);


//    @Query("""
//        SELECT a FROM Appointment a
//        JOIN FETCH a.doctor d
//        JOIN FETCH a.patient p
//        JOIN FETCH a.slot s
//        LEFT JOIN FETCH d.userProfile dp
//        LEFT JOIN FETCH p.userProfile pp
//        LEFT JOIN FETCH d.medicalProfile mp
//        LEFT JOIN FETCH mp.specialty
//        WHERE (:status IS NULL OR a.status = :status)
//    """)
//    Page<Appointment> findAllAppointmentsByStatus(
//            @Param("status") Status status,
//            Pageable pageable);

//    @Query("""
//        SELECT a FROM Appointment a
//        JOIN FETCH a.doctor d
//        JOIN FETCH a.patient p
//        JOIN FETCH a.slot s
//        LEFT JOIN FETCH d.userProfile dp
//        LEFT JOIN FETCH p.userProfile pp
//        LEFT JOIN FETCH d.medicalProfile mp
//        LEFT JOIN FETCH mp.specialty
//        WHERE (:userId IS NULL OR a.patient.id = :userId OR a.doctor.id = :userId)
//        AND (:status IS NULL OR a.status = :status)
//    """)
//    Page<Appointment> findAppointmentsByUserIdAndStatus(
//            @Param("userId") UUID userId,
//            @Param("status") Status status,
//            Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.status = 'PENDING' AND a.createdAt < :expiredTime")
    List<Appointment> findExpiredPendingAppointments(@Param("expiredTime") LocalDateTime expiredTime);

//    @Query("SELECT a FROM Appointment a " +
//            "JOIN a.slot s " +
//            "WHERE s.doctor.id = :doctorUserId " +
//            "AND s.slotDate = :appointmentDate " +
//            "AND a.status IN :statuses " +
//            "ORDER BY s.startTime")
//    List<Appointment> findAppointmentsByDoctorAndFullDayOptimal(
//            @Param("doctorUserId") UUID doctorUserId,
//            @Param("appointmentDate") LocalDate appointmentDate,
//            @Param("statuses") List<Status> statuses
//    );

//    @Query("SELECT a FROM Appointment a " +
//            "JOIN a.slot s " +
//            "WHERE s.doctor.id = :doctorUserId " +
//            "AND s.slotDate = :appointmentDate " +
//            "AND NOT (s.endTime <= :startTime OR s.startTime >= :endTime) " +
//            "AND a.status IN :statuses " +
//            "ORDER BY s.startTime")
//    List<Appointment> findOverlappingAppointments(
//            @Param("doctorUserId") UUID doctorUserId,
//            @Param("appointmentDate") LocalDate appointmentDate,
//            @Param("startTime") LocalTime startTime,
//            @Param("endTime") LocalTime endTime,
//            @Param("statuses") List<Status> statuses
//    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.id = :appointmentId")
    Optional<Appointment> findByIdWithLock(@Param("appointmentId") UUID appointmentId);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND a.appointmentDate < :date")
    List<Appointment> findAppointmentsToAutoComplete(
            @Param("status") Status status,
            @Param("date") LocalDate date
    );
}
