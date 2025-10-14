package org.project.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserProfileResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class UserProfileServiceClient extends BaseServiceClient {

    @Value("${service.user-profile.url}")
    private String userProfileUrl;

    public UserProfileServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "User Profile Service";
    }

    public UserProfileResponse getUserProfile(UUID userId) {
        String url = String.format("%s/api/internal/user-profile/user/%s",userProfileUrl, userId);
        return get(url, UserProfileResponse.class);
    }
}
