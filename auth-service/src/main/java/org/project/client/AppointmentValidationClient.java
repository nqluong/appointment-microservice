package org.project.client;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Component
@Slf4j
public class AppointmentValidationClient extends BaseServiceClient {

    @Value("${service.appointment.url}")
    private String appointmentServiceUrl;

    public AppointmentValidationClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "Appointment Service";
    }

    public boolean checkOverlappingAppointment(UUID patientId, LocalDate appointmentDate, LocalTime startTime, LocalTime endTime) {
        String url = UriComponentsBuilder
                .fromHttpUrl(appointmentServiceUrl)
                .path("/api/internal/appointments/check-overlapping")
                .queryParam("patientId", patientId)
                .queryParam("appointmentDate", appointmentDate)
                .queryParam("startTime", startTime)
                .queryParam("endTime", endTime)
                .toUriString();
        return get(url, Boolean.class);
    }

    public int countPendingAppointments(UUID patientId){
        String url = String.format("%s/api/internal/appointments/count-pending/%s", appointmentServiceUrl, patientId);
        return get(url, Integer.class);
    }
}
