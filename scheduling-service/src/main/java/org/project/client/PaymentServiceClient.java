package org.project.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.request.PaymentRefundRequest;
import org.project.enums.RefundType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {
    private final RestTemplate restTemplate;

    @Value("${service.payment-service.url}")
    private String paymentServiceUrl;


    public void refundPaymentForAppointment(UUID appointmentId, String reason) {
        try {
            String url = String.format("%s/api/payments/refund/appointment/%s",
                    paymentServiceUrl, appointmentId);

            PaymentRefundRequest request = PaymentRefundRequest.builder()
                    .appointmentId(appointmentId)
                    .reason(reason)
                    .refundType(RefundType.FULL_REFUND)
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentRefundRequest> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.info("Đã hoàn tiền cho appointment ID: {}", appointmentId);
        } catch (Exception e) {
            log.error("Lỗi khi hoàn tiền cho appointment ID: {}. Chi tiết: {}",
                    appointmentId, e.getMessage(), e);
        }
    }
}
