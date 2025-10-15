package org.project.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserBasicInfoResponse;
import org.project.dto.response.UserValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Slf4j
@Component
public class AuthServiceClient extends BaseServiceClient {

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "Auth Service";
    }

    /**
     * Validate user với role cụ thể
     */
    public UserValidationResponse validateUser(UUID userId, String requiredRole) {
        String url = String.format("%s/api/internal/users/%s/validate?role=%s",
                authServiceUrl, userId, requiredRole);

        return get(url, UserValidationResponse.class);
    }

    /**
     * Get basic user info
     */
    public UserBasicInfoResponse getUserBasicInfo(UUID userId) {
        String url = authServiceUrl + "/api/internal/users/" + userId + "/basic";
        return get(url, UserBasicInfoResponse.class);
    }
}
