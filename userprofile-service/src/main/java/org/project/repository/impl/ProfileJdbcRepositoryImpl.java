//package org.project.repository.impl;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.project.appointment_project.user.dto.request.ProfileUpdateRequest;
//import org.project.appointment_project.user.dto.request.UpdateMedicalProfileRequest;
//import org.project.appointment_project.user.dto.request.UpdateUserProfileRequest;
//import org.project.appointment_project.user.enums.Gender;
//import org.project.appointment_project.user.model.MedicalProfile;
//import org.project.appointment_project.user.model.User;
//import org.project.appointment_project.user.model.UserProfile;
//import org.project.appointment_project.user.repository.ProfileJdbcRepository;
//import org.springframework.dao.EmptyResultDataAccessException;
//import org.springframework.jdbc.core.RowMapper;
//import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.stereotype.Repository;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//@RequiredArgsConstructor
//@Slf4j
//public class ProfileJdbcRepositoryImpl implements ProfileJdbcRepository {
//
//    private final NamedParameterJdbcTemplate namedJdbcTemplate;
//
//    private static final String SELECT_USER_PROFILE_SQL = """
//        SELECT id, user_id, first_name, last_name, date_of_birth, gender,
//               address, phone, avatar_url, created_at, updated_at
//        FROM user_profiles WHERE user_id = :userId
//        """;
//
//    private static final String SELECT_MEDICAL_PROFILE_SQL = """
//        SELECT id, user_id, blood_type, allergies, medical_history,
//               emergency_contact_name, emergency_contact_phone, license_number,
//               qualification, years_of_experience, consultation_fee, bio,
//               is_doctor_approved, specialty_id, created_at, updated_at
//        FROM medical_profiles WHERE user_id = :userId
//        """;
//
//    private static final String SELECT_COMPLETE_USER_PROFILE_SQL = """
//        SELECT u.id as user_id, u.username, u.email, u.is_active, u.is_email_verified,
//               u.created_at as user_created_at, u.updated_at as user_updated_at,
//
//               up.id as profile_id, up.first_name, up.last_name, up.date_of_birth,
//               up.gender, up.address, up.phone, up.avatar_url,
//               up.created_at as profile_created_at, up.updated_at as profile_updated_at,
//
//               mp.id as medical_id, mp.blood_type, mp.allergies, mp.medical_history,
//               mp.emergency_contact_name, mp.emergency_contact_phone, mp.license_number,
//               mp.qualification, mp.years_of_experience, mp.consultation_fee, mp.bio,
//               mp.is_doctor_approved, mp.specialty_id,
//               mp.created_at as medical_created_at, mp.updated_at as medical_updated_at
//
//        FROM users u
//        LEFT JOIN user_profiles up ON u.id = up.user_id
//        LEFT JOIN medical_profiles mp ON u.id = mp.user_id
//        WHERE u.id = :userId
//        """;
//
//    private static final String INSERT_USER_PROFILE_SQL = """
//        INSERT INTO user_profiles (id, user_id, first_name, last_name, date_of_birth,
//                                  gender, address, phone, avatar_url, created_at, updated_at)
//        VALUES (:id, :userId, :firstName, :lastName, :dateOfBirth,
//                :gender, :address, :phone, :avatarUrl, :createdAt, :updatedAt)
//        """;
//
//    private static final String INSERT_MEDICAL_PROFILE_SQL = """
//        INSERT INTO medical_profiles (id, user_id, blood_type, allergies, medical_history,
//                                     emergency_contact_name, emergency_contact_phone,
//                                     license_number, qualification, years_of_experience,
//                                     consultation_fee, bio, specialty_id, is_doctor_approved,
//                                     created_at, updated_at)
//        VALUES (:id, :userId, :bloodType, :allergies, :medicalHistory,
//                :emergencyContactName, :emergencyContactPhone,
//                :licenseNumber, :qualification, :yearsOfExperience,
//                :consultationFee, :bio, :specialtyId, :isDoctorApproved,
//                :createdAt, :updatedAt)
//        """;
//
//    private static final String UPDATE_USER_PROFILE_SQL = """
//        UPDATE user_profiles
//        SET first_name = COALESCE(:firstName, first_name),
//            last_name = COALESCE(:lastName, last_name),
//            date_of_birth = COALESCE(:dateOfBirth, date_of_birth),
//            gender = COALESCE(:gender, gender),
//            address = COALESCE(:address, address),
//            phone = COALESCE(:phone, phone),
//            avatar_url = COALESCE(:avatarUrl, avatar_url),
//            updated_at = :updatedAt
//        WHERE user_id = :userId
//        """;
//
//    private static final String UPDATE_MEDICAL_PROFILE_SQL = """
//        UPDATE medical_profiles
//        SET blood_type = COALESCE(:bloodType, blood_type),
//            allergies = COALESCE(:allergies, allergies),
//            medical_history = COALESCE(:medicalHistory, medical_history),
//            emergency_contact_name = COALESCE(:emergencyContactName, emergency_contact_name),
//            emergency_contact_phone = COALESCE(:emergencyContactPhone, emergency_contact_phone),
//            license_number = COALESCE(:licenseNumber, license_number),
//            qualification = COALESCE(:qualification, qualification),
//            years_of_experience = COALESCE(:yearsOfExperience, years_of_experience),
//            consultation_fee = COALESCE(:consultationFee, consultation_fee),
//            bio = COALESCE(:bio, bio),
//            specialty_id = COALESCE(:specialtyId, specialty_id),
//            is_doctor_approved = COALESCE(:isDoctorApproved, is_doctor_approved),
//            updated_at = :updatedAt
//        WHERE user_id = :userId
//        """;
//
//    private static final String EXISTS_USER_PROFILE_SQL =
//            "SELECT COUNT(1) FROM user_profiles WHERE user_id = :userId";
//
//    private static final String EXISTS_MEDICAL_PROFILE_SQL =
//            "SELECT COUNT(1) FROM medical_profiles WHERE user_id = :userId";
//
//
//    //Cập nhật thông tin người dùng
//    @Override
//    @Transactional
//    public boolean updateProfile(UUID userId, ProfileUpdateRequest request) {
//        try {
//            boolean hasUpdates = false;
//
//            //  Cập nhật bảng UserProfile
//            if (hasUserProfileFields(request)) {
//                hasUpdates |= updateUserProfileTable(userId, request);
//            }
//
//            //  Cập nhật bảng Medical Profile
//            if (hasMedicalProfileFields(request)) {
//                hasUpdates |= updateMedicalProfileTable(userId, request);
//            }
//
//            log.info("Profile update completed for userId: {}, hasUpdates: {}", userId, hasUpdates);
//            return hasUpdates;
//
//        } catch (Exception e) {
//            log.error("Error updating profile for userId: {}", userId, e);
//            throw e;
//        }
//    }
//
//    //Lấy thông tin của người dùng qua userId
//    @Override
//    public Optional<User> getCompleteUserProfile(UUID userId) {
//        try {
//            MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//            User user = namedJdbcTemplate.queryForObject(SELECT_COMPLETE_USER_PROFILE_SQL, params, new CompleteUserRowMapper());
//            return Optional.ofNullable(user);
//        } catch (EmptyResultDataAccessException e) {
//            return Optional.empty();
//        }
//    }
//
//    @Override
//    public UUID createUserProfile(UUID userId, UpdateUserProfileRequest request) {
//        UUID profileId = UUID.randomUUID();
//        LocalDateTime now = LocalDateTime.now();
//
//        MapSqlParameterSource params = new MapSqlParameterSource()
//                .addValue("id", profileId)
//                .addValue("userId", userId)
//                .addValue("firstName", request.getFirstName())
//                .addValue("lastName", request.getLastName())
//                .addValue("dateOfBirth", request.getDateOfBirth())
//                .addValue("gender", request.getGender() != null ? request.getGender().name() : null)
//                .addValue("address", request.getAddress())
//                .addValue("phone", request.getPhone())
//                .addValue("avatarUrl", request.getAvatarUrl())
//                .addValue("createdAt", now)
//                .addValue("updatedAt", now);
//
//        namedJdbcTemplate.update(INSERT_USER_PROFILE_SQL, params);
//        log.debug("Created user profile with id: {} for userId: {}", profileId, userId);
//        return profileId;
//    }
//
//    @Override
//    public UUID createMedicalProfile(UUID userId, UpdateMedicalProfileRequest request) {
//        UUID profileId = UUID.randomUUID();
//        LocalDateTime now = LocalDateTime.now();
//
//        MapSqlParameterSource params = new MapSqlParameterSource()
//                .addValue("id", profileId)
//                .addValue("userId", userId)
//                .addValue("bloodType", request.getBloodType())
//                .addValue("allergies", request.getAllergies())
//                .addValue("medicalHistory", request.getMedicalHistory())
//                .addValue("emergencyContactName", request.getEmergencyContactName())
//                .addValue("emergencyContactPhone", request.getEmergencyContactPhone())
//                .addValue("createdAt", now)
//                .addValue("updatedAt", now);
//
//        namedJdbcTemplate.update(INSERT_MEDICAL_PROFILE_SQL, params);
//        log.debug("Created medical profile with id: {} for userId: {}", profileId, userId);
//        return profileId;
//    }
//
//    //Tìm kiếm userProfile theo userId
//    @Override
//    public Optional<UserProfile> findUserProfileByUserId(UUID userId) {
//        try {
//            MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//            UserProfile userProfile = namedJdbcTemplate.queryForObject(SELECT_USER_PROFILE_SQL, params, new UserProfileRowMapper());
//            return Optional.ofNullable(userProfile);
//        } catch (EmptyResultDataAccessException e) {
//            log.debug("No user profile found for userId: {}", userId);
//            return Optional.empty();
//        }
//    }
//
//    //Tìm kiếm medicalProfile theo userId
//    @Override
//    public Optional<MedicalProfile> findMedicalProfileByUserId(UUID userId) {
//        try {
//            MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//            MedicalProfile medicalProfile = namedJdbcTemplate.queryForObject(SELECT_MEDICAL_PROFILE_SQL, params, new MedicalProfileRowMapper());
//            return Optional.ofNullable(medicalProfile);
//        } catch (EmptyResultDataAccessException e) {
//            log.debug("No medical profile found for userId: {}", userId);
//            return Optional.empty();
//        }
//    }
//
//    @Override
//    public boolean existsUserProfile(UUID userId) {
//        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//        Integer count = namedJdbcTemplate.queryForObject(EXISTS_USER_PROFILE_SQL, params, Integer.class);
//        return count != null && count > 0;
//    }
//
//    @Override
//    public boolean existsMedicalProfile(UUID userId) {
//        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//        Integer count = namedJdbcTemplate.queryForObject(EXISTS_MEDICAL_PROFILE_SQL, params, Integer.class);
//        return count != null && count > 0;
//    }
//
//    private boolean updateUserProfileTable(UUID userId, ProfileUpdateRequest request) {
//        // Tạo mới nếu chưa tồn tại
//        if (!existsUserProfile(userId)) {
//            return createUserProfile(userId, request);
//        }
//
//        List<String> setClauses = new ArrayList<>();
//        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//
//        addFieldToUpdate(setClauses, params, "firstName", request.getFirstName(), "first_name = :firstName");
//        addFieldToUpdate(setClauses, params, "lastName", request.getLastName(), "last_name = :lastName");
//        addFieldToUpdate(setClauses, params, "dateOfBirth", request.getDateOfBirth(), "date_of_birth = :dateOfBirth");
//        addFieldToUpdate(setClauses, params, "gender",
//                request.getGender() != null ? request.getGender().name() : null, "gender = :gender");
//        addFieldToUpdate(setClauses, params, "address", request.getAddress(), "address = :address");
//        addFieldToUpdate(setClauses, params, "phone", request.getPhone(), "phone = :phone");
//
//        if (setClauses.isEmpty()) {
//            log.debug("No user profile fields to update for userId: {}", userId);
//            return false;
//        }
//
//        String sql = "UPDATE user_profiles SET " + String.join(", ", setClauses) +
//                ", updated_at = :updatedAt WHERE user_id = :userId";
//        params.addValue("updatedAt", LocalDateTime.now());
//
//        int rowsAffected = namedJdbcTemplate.update(sql, params);
//        log.debug("Updated {} user profile rows for userId: {}", rowsAffected, userId);
//        return rowsAffected > 0;
//    }
//
//    private boolean updateMedicalProfileTable(UUID userId, ProfileUpdateRequest request) {
//        // Tạo mới khi chưa tồn tại
//        if (!existsMedicalProfile(userId)) {
//            return createMedicalProfile(userId, request);
//        }
//
//        List<String> setClauses = new ArrayList<>();
//        MapSqlParameterSource params = new MapSqlParameterSource("userId", userId);
//
//        // Cập nhật các field medical
//        addFieldToUpdate(setClauses, params, "bloodType", request.getBloodType(), "blood_type = :bloodType");
//        addFieldToUpdate(setClauses, params, "allergies", request.getAllergies(), "allergies = :allergies");
//        addFieldToUpdate(setClauses, params, "medicalHistory", request.getMedicalHistory(), "medical_history = :medicalHistory");
//        addFieldToUpdate(setClauses, params, "emergencyContactName", request.getEmergencyContactName(), "emergency_contact_name = :emergencyContactName");
//        addFieldToUpdate(setClauses, params, "emergencyContactPhone", request.getEmergencyContactPhone(), "emergency_contact_phone = :emergencyContactPhone");
//
//        // Các field chuyên môn
//        addFieldToUpdate(setClauses, params, "licenseNumber", request.getLicenseNumber(), "license_number = :licenseNumber");
//        addFieldToUpdate(setClauses, params, "qualification", request.getQualification(), "qualification = :qualification");
//        addFieldToUpdate(setClauses, params, "yearsOfExperience", request.getYearsOfExperience(), "years_of_experience = :yearsOfExperience");
//        addFieldToUpdate(setClauses, params, "consultationFee", request.getConsultationFee(), "consultation_fee = :consultationFee");
//        addFieldToUpdate(setClauses, params, "bio", request.getBio(), "bio = :bio");
//        addFieldToUpdate(setClauses, params, "isDoctorApproved", request.getIsDoctorApproved(), "is_doctor_approved = :isDoctorApproved");
//
//        // Cập nhật riêng chuyên khoa
//        if (StringUtils.hasText(request.getSpecialtyId())) {
//            try {
//                UUID specialtyId = UUID.fromString(request.getSpecialtyId());
//                setClauses.add("specialty_id = :specialtyId");
//                params.addValue("specialtyId", specialtyId);
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid specialty ID format: {}", request.getSpecialtyId());
//            }
//        }
//
//        if (setClauses.isEmpty()) {
//            log.debug("No medical profile fields to update for userId: {}", userId);
//            return false;
//        }
//
//        String sql = "UPDATE medical_profiles SET " + String.join(", ", setClauses) +
//                ", updated_at = :updatedAt WHERE user_id = :userId";
//        params.addValue("updatedAt", LocalDateTime.now());
//
//        int rowsAffected = namedJdbcTemplate.update(sql, params);
//        log.debug("Updated {} medical profile rows for userId: {}", rowsAffected, userId);
//        return rowsAffected > 0;
//    }
//
//
//    private boolean createUserProfile(UUID userId, ProfileUpdateRequest request) {
//        if (!hasUserProfileFields(request)) {
//            return false;
//        }
//
//        String sql = """
//            INSERT INTO user_profiles (id, user_id, first_name, last_name, date_of_birth,
//                                      gender, address, phone, avatar_url, created_at, updated_at)
//            VALUES (:id, :userId, :firstName, :lastName, :dateOfBirth,
//                    :gender, :address, :phone, :avatarUrl, :createdAt, :updatedAt)
//            """;
//
//        MapSqlParameterSource params = new MapSqlParameterSource()
//                .addValue("id", UUID.randomUUID())
//                .addValue("userId", userId)
//                .addValue("firstName", request.getFirstName())
//                .addValue("lastName", request.getLastName())
//                .addValue("dateOfBirth", request.getDateOfBirth())
//                .addValue("gender", request.getGender() != null ? request.getGender().name() : null)
//                .addValue("address", request.getAddress())
//                .addValue("phone", request.getPhone())
//                .addValue("avatarUrl", null) // ProfileUpdateRequest doesn't have avatarUrl
//                .addValue("createdAt", LocalDateTime.now())
//                .addValue("updatedAt", LocalDateTime.now());
//
//        int rowsAffected = namedJdbcTemplate.update(sql, params);
//        log.debug("Created user profile for userId: {}, rows: {}", userId, rowsAffected);
//        return rowsAffected > 0;
//    }
//
//    /**
//     * Create MedicalProfile from ProfileUpdateRequest
//     */
//    private boolean createMedicalProfile(UUID userId, ProfileUpdateRequest request) {
//        String sql = """
//            INSERT INTO medical_profiles (id, user_id, blood_type, allergies, medical_history,
//                                         emergency_contact_name, emergency_contact_phone,
//                                         license_number, qualification, years_of_experience,
//                                         consultation_fee, bio, specialty_id, is_doctor_approved,
//                                         created_at, updated_at)
//            VALUES (:id, :userId, :bloodType, :allergies, :medicalHistory,
//                    :emergencyContactName, :emergencyContactPhone,
//                    :licenseNumber, :qualification, :yearsOfExperience,
//                    :consultationFee, :bio, :specialtyId, :isDoctorApproved,
//                    :createdAt, :updatedAt)
//            """;
//
//        MapSqlParameterSource params = new MapSqlParameterSource()
//                .addValue("id", UUID.randomUUID())
//                .addValue("userId", userId)
//                .addValue("bloodType", request.getBloodType())
//                .addValue("allergies", request.getAllergies())
//                .addValue("medicalHistory", request.getMedicalHistory())
//                .addValue("emergencyContactName", request.getEmergencyContactName())
//                .addValue("emergencyContactPhone", request.getEmergencyContactPhone())
//                .addValue("licenseNumber", request.getLicenseNumber())
//                .addValue("qualification", request.getQualification())
//                .addValue("yearsOfExperience", request.getYearsOfExperience())
//                .addValue("consultationFee", request.getConsultationFee())
//                .addValue("bio", request.getBio())
//                .addValue("isDoctorApproved", request.getIsDoctorApproved() != null ? request.getIsDoctorApproved() : false)
//                .addValue("createdAt", LocalDateTime.now())
//                .addValue("updatedAt", LocalDateTime.now());
//
//        // Handle specialty
//        UUID specialtyId = null;
//        if (StringUtils.hasText(request.getSpecialtyId())) {
//            try {
//                specialtyId = UUID.fromString(request.getSpecialtyId());
//            } catch (IllegalArgumentException e) {
//                log.warn("Invalid specialty ID format: {}", request.getSpecialtyId());
//            }
//        }
//        params.addValue("specialtyId", specialtyId);
//
//        int rowsAffected = namedJdbcTemplate.update(sql, params);
//        log.debug("Created medical profile for userId: {}, rows: {}", userId, rowsAffected);
//        return rowsAffected > 0;
//    }
//
//
//    //Thêm trường vào mệnh đề UPDATE chỉ khi giá trị không null/rỗng
//    private void addFieldToUpdate(List<String> setClauses, MapSqlParameterSource params,
//                                  String paramName, Object value, String sqlClause) {
//        if (value != null && (!(value instanceof String) || StringUtils.hasText((String) value))) {
//            setClauses.add(sqlClause);
//            params.addValue(paramName, value);
//        }
//    }
//
//    private boolean hasUserProfileFields(ProfileUpdateRequest request) {
//        return StringUtils.hasText(request.getFirstName()) ||
//                StringUtils.hasText(request.getLastName()) ||
//                request.getDateOfBirth() != null ||
//                request.getGender() != null ||
//                StringUtils.hasText(request.getAddress()) ||
//                StringUtils.hasText(request.getPhone());
//    }
//
//    private boolean hasMedicalProfileFields(ProfileUpdateRequest request) {
//        return StringUtils.hasText(request.getBloodType()) ||
//                StringUtils.hasText(request.getAllergies()) ||
//                StringUtils.hasText(request.getMedicalHistory()) ||
//                StringUtils.hasText(request.getEmergencyContactName()) ||
//                StringUtils.hasText(request.getEmergencyContactPhone()) ||
//                StringUtils.hasText(request.getLicenseNumber()) ||
//                StringUtils.hasText(request.getQualification()) ||
//                request.getYearsOfExperience() != null ||
//                request.getConsultationFee() != null ||
//                StringUtils.hasText(request.getBio()) ||
//                StringUtils.hasText(request.getSpecialtyId()) ||
//                request.getIsDoctorApproved() != null;
//    }
//
//    //RowMapper cho UserProfile
//    private static class UserProfileRowMapper implements RowMapper<UserProfile> {
//        @Override
//        public UserProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
//            return UserProfile.builder()
//                    .id(UUID.fromString(rs.getString("id")))
//                    .firstName(rs.getString("first_name"))
//                    .lastName(rs.getString("last_name"))
//                    .dateOfBirth(rs.getDate("date_of_birth") != null ?
//                            rs.getDate("date_of_birth").toLocalDate() : null)
//                    .gender(rs.getString("gender") != null ?
//                            Gender.valueOf(rs.getString("gender")) : null)
//                    .address(rs.getString("address"))
//                    .phone(rs.getString("phone"))
//                    .avatarUrl(rs.getString("avatar_url"))
//                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
//                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
//                    .build();
//        }
//    }
//
//    //RowMapper cho MedicalProfile
//    private static class MedicalProfileRowMapper implements RowMapper<MedicalProfile> {
//        @Override
//        public MedicalProfile mapRow(ResultSet rs, int rowNum) throws SQLException {
//            return MedicalProfile.builder()
//                    .id(UUID.fromString(rs.getString("id")))
//                    .bloodType(rs.getString("blood_type"))
//                    .allergies(rs.getString("allergies"))
//                    .medicalHistory(rs.getString("medical_history"))
//                    .emergencyContactName(rs.getString("emergency_contact_name"))
//                    .emergencyContactPhone(rs.getString("emergency_contact_phone"))
//                    .licenseNumber(rs.getString("license_number"))
//                    .qualification(rs.getString("qualification"))
//                    .yearsOfExperience(rs.getObject("years_of_experience", Integer.class))
//                    .consultationFee(rs.getBigDecimal("consultation_fee"))
//                    .bio(rs.getString("bio"))
//                    .isDoctorApproved(rs.getBoolean("is_doctor_approved"))
//                    .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
//                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
//                    .build();
//        }
//    }
//
//
//    //RowMapper cho  User profile từ câu query
//    private static class CompleteUserRowMapper implements RowMapper<User> {
//        @Override
//        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
//            User user = User.builder()
//                    .id(UUID.fromString(rs.getString("user_id")))
//                    .username(rs.getString("username"))
//                    .email(rs.getString("email"))
//                    .isActive(rs.getBoolean("is_active"))
//                    .isEmailVerified(rs.getBoolean("is_email_verified"))
//                    .createdAt(rs.getTimestamp("user_created_at").toLocalDateTime())
//                    .updatedAt(rs.getTimestamp("user_updated_at").toLocalDateTime())
//                    .build();
//
//            // Map UserProfile nếu tồn tại
//            if (rs.getString("profile_id") != null) {
//                UserProfile userProfile = UserProfile.builder()
//                        .id(UUID.fromString(rs.getString("profile_id")))
//                        .firstName(rs.getString("first_name"))
//                        .lastName(rs.getString("last_name"))
//                        .dateOfBirth(rs.getDate("date_of_birth") != null ?
//                                rs.getDate("date_of_birth").toLocalDate() : null)
//                        .gender(rs.getString("gender") != null ?
//                                Gender.valueOf(rs.getString("gender")) : null)
//                        .address(rs.getString("address"))
//                        .phone(rs.getString("phone"))
//                        .avatarUrl(rs.getString("avatar_url"))
//                        .createdAt(rs.getTimestamp("profile_created_at").toLocalDateTime())
//                        .updatedAt(rs.getTimestamp("profile_updated_at").toLocalDateTime())
//                        .user(user)
//                        .build();
//                user.setUserProfile(userProfile);
//            }
//
//            // Map MedicalProfile nếu tồn tại
//            if (rs.getString("medical_id") != null) {
//                MedicalProfile medicalProfile = MedicalProfile.builder()
//                        .id(UUID.fromString(rs.getString("medical_id")))
//                        .bloodType(rs.getString("blood_type"))
//                        .allergies(rs.getString("allergies"))
//                        .medicalHistory(rs.getString("medical_history"))
//                        .emergencyContactName(rs.getString("emergency_contact_name"))
//                        .emergencyContactPhone(rs.getString("emergency_contact_phone"))
//                        .licenseNumber(rs.getString("license_number"))
//                        .qualification(rs.getString("qualification"))
//                        .yearsOfExperience(rs.getObject("years_of_experience", Integer.class))
//                        .consultationFee(rs.getBigDecimal("consultation_fee"))
//                        .bio(rs.getString("bio"))
//                        .isDoctorApproved(rs.getBoolean("is_doctor_approved"))
//                        .createdAt(rs.getTimestamp("medical_created_at").toLocalDateTime())
//                        .updatedAt(rs.getTimestamp("medical_updated_at").toLocalDateTime())
//                        .user(user)
//                        .build();
//                user.setMedicalProfile(medicalProfile);
//            }
//            return user;
//        }
//    }
//}
