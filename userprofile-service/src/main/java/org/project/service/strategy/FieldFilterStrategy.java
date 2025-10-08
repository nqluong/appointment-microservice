package org.project.service.strategy;


import org.project.dto.request.ProfileUpdateRequest;

import java.util.Set;

public interface FieldFilterStrategy {
    /**
     * Filter request để chỉ giữ lại những field được phép theo role
     */
    ProfileUpdateRequest filterFields(ProfileUpdateRequest request);

    boolean supports(Set<String> roles);


    Set<String> getAllowedUserProfileFields();


    Set<String> getAllowedMedicalProfileFields();
}
