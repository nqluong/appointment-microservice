package org.project.client;

import java.util.UUID;

import org.project.dto.response.AppointmentInternalResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AppointmentServiceClient extends BaseServiceClient {

    @Value("${services.appointment.url}")
    private String appointmentServiceUrl;

    public AppointmentServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "Appointment Service";
    }

    /**
     * Lấy thông tin appointment để tính toán payment amount
     */
    public AppointmentInternalResponse getAppointment(UUID appointmentId) {
        String url = appointmentServiceUrl + "/api/internal/appointments/" + appointmentId;
        log.info("Lấy thông tin appointment: {}", appointmentId);

        return get(url, AppointmentInternalResponse.class);
    }
}

