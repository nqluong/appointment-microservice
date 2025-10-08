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
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {
    private final RestTemplate restTemplate;

    @Value("${auth-service.url}")
    private String authServiceUrl;

    public UserIdsResponse getUserIdsByRole(String roleName) {
        String url = String.format("%s/api/internal/users/by-role/%s", authServiceUrl, roleName);
        try {
            UserIdsResponse response = restTemplate.getForObject(url, UserIdsResponse.class);

            if (response == null || response.getUserIds() == null) {
                throw new ExternalServiceException("Không nhận được phản hồi từ Auth-Service");
            }
            return response;

        } catch (Exception e) {
            log.error("Lỗi không mong đợi khi gọi Auth-Service", e);
            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi Auth-Service: %s", e.getMessage()));
        }
    }

    public boolean checkExistsbyId(UUID userId){
        String url = String.format("%s/api/internal/users/exists/%s", authServiceUrl, userId);

        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    throw new CustomException(ErrorCode.USER_NOT_FOUND);
                }
                throw new CustomException(ErrorCode.PHOTO_UPLOAD_ERROR);
            }
            return true;

        } catch (Exception e) {
            log.error("Lỗi không mong đợi khi gọi Auth-Service", e);
            throw new ExternalServiceException(
                    String.format("Lỗi không xác định khi gọi Auth-Service: %s", e.getMessage()));
        }
         // Hoặc UserDto.class nếu cần dữ liệu
    }
}
