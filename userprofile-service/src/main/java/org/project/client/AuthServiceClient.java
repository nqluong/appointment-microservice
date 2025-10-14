package org.project.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserBasicInfoResponse;
import org.project.dto.response.UserIdsResponse;
    import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@Slf4j
public class AuthServiceClient extends BaseServiceClient {

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public AuthServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "Auth-Service";
    }

    public UserBasicInfoResponse getUserBasicInfo(UUID userId) {
        String url = String.format("%s/api/internal/users/%s/basic", authServiceUrl, userId);
        return get(url, UserBasicInfoResponse.class);
    }

    public UserIdsResponse getUserIdsByRole(String roleName) {
        String url = String.format("%s/api/internal/users/by-role/%s", authServiceUrl, roleName);
        return get(url, UserIdsResponse.class);
    }

    public boolean checkExistsbyId(UUID userId){
        String url = String.format("%s/api/internal/users/exists/%s", authServiceUrl, userId);
        return get(url, Boolean.class);
    }

    public boolean isUserActive(UUID userId) {
        String url = String.format("%s/api/internal/users/active/%s", authServiceUrl, userId);
        return get(url, Boolean.class);
    }

    public boolean hasRole(UUID userId, String roleName) {
        String url = String.format("%s/api/internal/users/%s/has-role/%s", authServiceUrl, userId, roleName);
        return get(url, Boolean.class);
    }
}
