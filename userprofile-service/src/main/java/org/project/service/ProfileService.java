package org.project.service;



import org.project.dto.request.ProfileUpdateRequest;
import org.project.dto.response.CompleteProfileResponse;

import java.util.UUID;

public interface ProfileService {

    CompleteProfileResponse getCompleteProfile(UUID userId);

    CompleteProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);
}
