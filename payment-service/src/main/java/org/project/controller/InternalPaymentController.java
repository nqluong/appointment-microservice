package org.project.controller;

import java.util.UUID;

import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.response.PaymentUrlResponse;
import org.project.enums.PaymentStatus;
import org.project.repository.PaymentRepository;
import org.project.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PaymentRepository paymentRepository;

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
    

    @GetMapping("/appointment/{appointmentId}/has-processing")
    public ResponseEntity<Boolean> hasProcessingPayment(@PathVariable UUID appointmentId) {
        log.debug("Kiểm tra payment PROCESSING cho appointment: {}", appointmentId);
        
        boolean hasProcessing = paymentRepository.existsByAppointmentIdAndPaymentStatus(
                appointmentId, PaymentStatus.PROCESSING);
        
        log.debug("Appointment {} có payment PROCESSING: {}", appointmentId, hasProcessing);
        return ResponseEntity.ok(hasProcessing);
    }
}
