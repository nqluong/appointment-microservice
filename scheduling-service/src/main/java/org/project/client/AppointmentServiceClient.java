package org.project.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.CancelAppointmentRequest;
import org.project.dto.response.AppointmentResponse;
import org.project.enums.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentServiceClient {

    private final RestTemplate restTemplate;

    @Value("@{service.appointment-service.url}")
    private String appointmentUrl;

    public List<AppointmentResponse> findAppointmentsByDoctorAndFullDay(
            UUID doctorId,
            LocalDate date,
            List<Status> statuses) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(appointmentUrl + "/api/appointments/affected/full-day")
                    .queryParam("doctorId", doctorId)
                    .queryParam("date", date);

            if (statuses != null && !statuses.isEmpty()) {
                List<String> statusNames = statuses.stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                builder.queryParam("statuses", String.join(",", statusNames));
            }

            String url = builder.toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<AppointmentResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<AppointmentResponse>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();

        } catch (Exception e) {
            log.error("Lỗi khi gọi appointment service (full day): {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public List<AppointmentResponse>findAffectedAppointments(
            UUID doctorId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            List<Status> statuses) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(appointmentUrl + "/api/internal/appointments/affected/time-range")
                    .queryParam("doctorId", doctorId)
                    .queryParam("date", date)
                    .queryParam("startTime", startTime)
                    .queryParam("endTime", endTime);

            if (statuses != null && !statuses.isEmpty()) {
                List<String> statusNames = statuses.stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
                builder.queryParam("statuses", String.join(",", statusNames));
            }
            String url = builder.toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<List<AppointmentResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<List<AppointmentResponse>>() {}
            );

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Lỗi khi gọi appointment service để tìm affected appointments: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public void cancelAppointment(UUID appointmentId, String reason) {
        try {
            String url = String.format("%s/api/internal/appointments/%s/cancel", appointmentUrl, appointmentId);

            CancelAppointmentRequest request = CancelAppointmentRequest.builder()
                    .reason(reason)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<CancelAppointmentRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("Đã hủy appointment ID: {}", appointmentId);
        } catch (Exception e) {
            log.error("Lỗi khi hủy appointment ID: {}. Chi tiết: {}", appointmentId, e.getMessage(), e);
            throw new RuntimeException("Không thể hủy appointment", e);
        }
    }

    /**
     * Hủy nhiều appointments cùng lúc
     */
    public void cancelMultipleAppointments(List<UUID> appointmentIds, String reason) {
        for (UUID appointmentId : appointmentIds) {
            try {
                cancelAppointment(appointmentId, reason);
            } catch (Exception e) {
                log.error("Lỗi khi hủy appointment ID: {}", appointmentId, e);
            }
        }
    }
}
