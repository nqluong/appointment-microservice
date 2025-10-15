package org.project.controller;

import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.response.PaymentUrlResponse;
import org.project.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/internal/payments")
@RequiredArgsConstructor
@Slf4j
public class InternalPaymentController {
    
    private final PaymentService paymentService;

    /**
     * Internal API để appointment service tạo payment URL
     */
    @PostMapping
    public ResponseEntity<PaymentUrlResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {

        String customerIp = getClientIp(httpRequest);

        PaymentUrlResponse response = paymentService.createPayment(request, customerIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = null;

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            clientIp = xForwardedFor.split(",")[0].trim();
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("X-Real-IP");
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        if ("0:0:0:0:0:0:0:1".equals(clientIp) || "::1".equals(clientIp)) {
            clientIp = "127.0.0.1";
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = "127.0.0.1";
        }

        log.debug("Resolved client IP: {}", clientIp);
        return clientIp;
    }
}
