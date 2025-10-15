package org.project.client;

import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.response.PaymentUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PaymentServiceClient extends BaseServiceClient {

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    public PaymentServiceClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected String getServiceName() {
        return "Payment Service";
    }

    public PaymentUrlResponse createPayment(CreatePaymentRequest request) {
        String url = paymentServiceUrl + "/api/internal/payments";
        log.info("Tạo payment URL cho appointment: {}", request.getAppointmentId());

        try {
            // Thêm header X-Forwarded-For để payment service có thể lấy IP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Forwarded-For", "127.0.0.1"); // Default IP cho internal call
            
            HttpEntity<CreatePaymentRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<PaymentUrlResponse> response = restTemplate.postForEntity(url, entity, PaymentUrlResponse.class);
            
            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Failed to create payment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error creating payment for appointment: {}", request.getAppointmentId(), e);
            throw new RuntimeException("Failed to create payment URL", e);
        }
    }
}


