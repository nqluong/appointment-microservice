package org.project.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserIdsResponse;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public UserIdsResponse getUserIdsByRole(String roleName) {
        String url = String.format("%s/api/internal/users/by-role/%s", authServiceUrl, roleName);
        return get(url, UserIdsResponse.class);
    }

    public boolean checkExistsbyId(UUID userId){
        String url = String.format("%s/api/internal/users/exists/%s", authServiceUrl, userId);
        return get(url, Boolean.class);

    }
}
