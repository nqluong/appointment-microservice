package org.project.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface DoctorProjection {
    UUID getUserId();
    String getFullName();
    String getGender();
    String getPhone();
    String getAvatarUrl();
    String getQualification();
    Integer getYearsOfExperience();
    BigDecimal getConsultationFee();
    String getSpecialtyName();
}
