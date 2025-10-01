//package org.project.repository;
//
//import org.project.appointment_project.user.model.User;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.UUID;
//
//@Repository
//public interface DoctorRepository extends JpaRepository<User, UUID> {
//
//    @Query("SELECT DISTINCT u FROM User u " +
//            "JOIN u.userRoles ur " +
//            "JOIN ur.role r " +
//            "JOIN u.userProfile up " +
//            "JOIN u.medicalProfile mp " +
//            "LEFT JOIN mp.specialty s " +
//            "WHERE r.name = 'DOCTOR' " +
//            "AND u.isActive = true " +
//            "AND ur.isActive = true " +
//            "AND mp.isDoctorApproved = true")
//    Page<User> findAllApprovedDoctors(Pageable pageable);
//
//    @Query("SELECT DISTINCT u FROM User u " +
//            "JOIN u.userRoles ur " +
//            "JOIN ur.role r " +
//            "JOIN u.userProfile up " +
//            "JOIN u.medicalProfile mp " +
//            "LEFT JOIN mp.specialty s " +
//            "WHERE r.name = 'DOCTOR' " +
//            "AND u.isActive = true " +
//            "AND ur.isActive = true " +
//            "AND mp.isDoctorApproved = true " +
//            "AND (COALESCE(:specialtyName, '') = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :specialtyName, '%')))")
//    Page<User> findDoctorsWithFilters(@Param("specialtyName") String specialtyName, Pageable pageable);
//}
