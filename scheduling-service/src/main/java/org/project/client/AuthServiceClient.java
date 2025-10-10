package org.project.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserIdsResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class AuthServiceClient extends BaseServiceClient{

    @Value("${service.auth-service.url}")
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

    public boolean checkExistsById(UUID userId){
        String url = String.format("%s/api/internal/users/%s/exists", authServiceUrl, userId);
        return get(url, Boolean.class);
    }


    public Boolean hasRole(UUID userId, String roleName) {
        String url = String.format("%s/api/internal/users/%s/has-role/%s",
                authServiceUrl, userId, roleName);
        return get(url, Boolean.class);
    }

    public Boolean hasAllRoles(UUID userId, List<String> roles) {
        String url = UriComponentsBuilder
                .fromHttpUrl(authServiceUrl)
                .path("/api/internal/users/{userId}/has-all-roles")
                .queryParam("roles", roles.toArray())
                .buildAndExpand(userId)
                .toUriString();

        return get(url, Boolean.class);
    }

    public Boolean hasAnyRole(UUID userId, List<String> roles) {
        String url = UriComponentsBuilder
                .fromHttpUrl(authServiceUrl)
                .path("/api/internal/users/{userId}/has-any-role")
                .queryParam("roles", roles.toArray())
                .buildAndExpand(userId)
                .toUriString();

        return get(url, Boolean.class);
    }

}
