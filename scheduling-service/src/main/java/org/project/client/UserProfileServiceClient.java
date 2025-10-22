package org.project.client;

import lombok.extern.slf4j.Slf4j;
import org.project.dto.PageResponse;
import org.project.dto.response.DoctorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class UserProfileServiceClient extends BaseServiceClient {

    @Value("${service.userprofile-service.url}")
    private String userProfileServiceUrl;

    public UserProfileServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "UserProfile-Service";
    }


    public DoctorResponse getDoctorById(UUID doctorId) {
        String url = String.format("%s/api/internal/medical-profile/validate-doctor/%s", 
                userProfileServiceUrl, doctorId);
        
        try {
            return get(url, DoctorResponse.class);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin bác sĩ {}: {}", doctorId, e.getMessage());
            return null;
        }
    }


    public PageResponse<DoctorResponse> getDoctors(int page, int size, String sortBy, String sortDir) {
        String url = UriComponentsBuilder
                .fromUriString(userProfileServiceUrl + "/api/internal/medical-profile/doctors")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .queryParam("sortDir", sortDir)
                .build()
                .toUriString();

        try {
            return get(url, new ParameterizedTypeReference<PageResponse<DoctorResponse>>() {});
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách bác sĩ: {}", e.getMessage());
            return PageResponse.<DoctorResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .page(page)
                    .size(size)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }
    }

    public PageResponse<DoctorResponse> getDoctorsBySpecialty(UUID specialtyId, int page, int size, String sortBy, String sortDir) {
        String url = UriComponentsBuilder
                .fromUriString(userProfileServiceUrl + "/api/internal/medical-profile/doctors/specialty/" + specialtyId)
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("sortBy", sortBy)
                .queryParam("sortDir", sortDir)
                .build()
                .toUriString();

        try {
            return get(url, new ParameterizedTypeReference<PageResponse<DoctorResponse>>() {});
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách bác sĩ theo chuyên khoa {}: {}", specialtyId, e.getMessage());
            return PageResponse.<DoctorResponse>builder()
                    .content(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .page(page)
                    .size(size)
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }
    }
}
