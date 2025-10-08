package org.project.repository;

import org.project.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    @Query("SELECT DISTINCT u.id FROM User u " +
            "JOIN u.userRoles ur " +
            "JOIN ur.role r " +
            "WHERE r.name = :roleName " +
            "AND u.isActive = true " +
            "AND ur.isActive = true")
    List<UUID> findUserIdsByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndDeletedAtIsNull(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.userRoles ur " +
            "LEFT JOIN FETCH ur.role " +
            "WHERE u.username = :username AND u.isActive = true")
    Optional<User> findActiveUserByUsernameWithRoles(@Param("username") String username);

    @Query("""
        SELECT DISTINCT u FROM User u 
        JOIN u.userRoles ur 
        JOIN ur.role r 
        WHERE r.name = 'DOCTOR' AND u.deletedAt IS NOT NULL
        ORDER BY u.deletedAt DESC
        """)
    Page<User> findDeletedDoctors(Pageable pageable);

    @Query("""
        SELECT DISTINCT u FROM User u 
        JOIN u.userRoles ur 
        JOIN ur.role r 
        WHERE r.name = 'PATIENT' AND u.deletedAt IS NOT NULL
        ORDER BY u.deletedAt DESC
        """)
    Page<User> findDeletedPatients(Pageable pageable);

    @Query("""
        SELECT u FROM User u 
        WHERE u.deletedAt IS NOT NULL
        ORDER BY u.deletedAt DESC
        """)
    Page<User> findAllDeletedUsers(Pageable pageable);

    @Query("""
        SELECT DISTINCT u FROM User u 
        JOIN u.userRoles ur 
        JOIN ur.role r 
        WHERE r.name = 'DOCTOR' AND u.deletedAt IS NULL AND u.isActive = true
        ORDER BY u.createdAt DESC
        """)
    Page<User> findActiveDoctors(Pageable pageable);

    @Query("""
        SELECT DISTINCT u FROM User u 
        JOIN u.userRoles ur 
        JOIN ur.role r 
        WHERE r.name = 'PATIENT' AND u.deletedAt IS NULL AND u.isActive = true
        ORDER BY u.createdAt DESC
        """)
    Page<User> findActivePatients(Pageable pageable);

    @Query("""
        SELECT u FROM User u 
        WHERE u.deletedAt IS NULL 
        AND u.isActive = true
        ORDER BY u.createdAt DESC
        """)
    Page<User> findAllActiveUsers(Pageable pageable);

//    @Query("""
//        SELECT DISTINCT u FROM User u
//        LEFT JOIN u.userRoles ur
//        LEFT JOIN ur.role r
//        LEFT JOIN u.userProfile up
//        WHERE (:keyword IS NULL OR
//               LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//               LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
//               LOWER(CONCAT(up.firstName, ' ', up.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')))
//        AND (:userType IS NULL OR :userType = 'ALL' OR r.name = :userType)
//        AND (:isDeleted IS NULL OR
//             (:isDeleted = true AND u.deletedAt IS NOT NULL) OR
//             (:isDeleted = false AND u.deletedAt IS NULL))
//        ORDER BY u.createdAt DESC
//        """)
//    Page<User> searchUsers(@Param("keyword") String keyword,
//                           @Param("userType") String userType,
//                           @Param("isDeleted") Boolean isDeleted,
//                           Pageable pageable);
//
}
