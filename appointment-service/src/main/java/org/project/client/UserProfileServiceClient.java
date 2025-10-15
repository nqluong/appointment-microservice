package org.project.client;

import java.util.UUID;

import org.project.dto.response.DoctorValidationResponse;
import org.project.dto.response.MedicalProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserProfileServiceClient extends BaseServiceClient {

    @Value("${services.userprofile.url}")
    private String userProfileServiceUrl;

    public UserProfileServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "UserProfile Service";
    }

    public MedicalProfileResponse getMedicalProfile(UUID userId) {
        String url = userProfileServiceUrl + "/api/internal/medical-profile/user/" + userId;
        log.debug("Lấy thông tin medical profile cho user {}", userId);

        return get(url, MedicalProfileResponse.class);
    }

    public DoctorValidationResponse validateDoctor(UUID doctorId) {
        String url = userProfileServiceUrl + "/api/internal/medical-profile/validate-doctor/" + doctorId;
        log.debug("Kiểm tra doctor {}", doctorId);

        return get(url, DoctorValidationResponse.class);
    }
}
