package org.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.CreatePaymentRequest;
import org.project.dto.request.PaymentCallbackRequest;
import org.project.dto.request.PaymentRefundRequest;
import org.project.dto.response.PaymentRefundResponse;
import org.project.dto.response.PaymentResponse;
import org.project.dto.response.PaymentUrlResponse;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentType;
import org.project.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentUrlResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            HttpServletRequest httpRequest) {

        String customerIp = getClientIp(httpRequest);

        PaymentUrlResponse response = paymentService.createPayment(request, customerIp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/deposit/{appointmentId}")
    public ResponseEntity<PaymentUrlResponse> createDepositPayment(
            @PathVariable UUID appointmentId,
            HttpServletRequest httpRequest) {

        String customerIp = getClientIp(httpRequest);

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .appointmentId(appointmentId)
                .paymentType(PaymentType.DEPOSIT)
                .paymentMethod(PaymentMethod.VNPAY)
                .build();

        PaymentUrlResponse response = paymentService.createPayment(request, customerIp);
        return ResponseEntity.ok(response);
    }

    // Convenience endpoint for full payment
    @PostMapping("/full/{appointmentId}")
    public ResponseEntity<PaymentUrlResponse> createFullPayment(
            @PathVariable UUID appointmentId,
            HttpServletRequest httpRequest) {

        String customerIp = getClientIp(httpRequest);

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .appointmentId(appointmentId)
                .paymentType(PaymentType.FULL)
                .paymentMethod(PaymentMethod.VNPAY)
                .build();

        PaymentUrlResponse response = paymentService.createPayment(request, customerIp);
        return ResponseEntity.ok(response);
    }

    // Convenience endpoint for remaining payment
    @PostMapping("/remaining/{appointmentId}")
    public ResponseEntity<PaymentUrlResponse> createRemainingPayment(
            @PathVariable UUID appointmentId,
            HttpServletRequest httpRequest) {

        String customerIp = getClientIp(httpRequest);

        CreatePaymentRequest request = CreatePaymentRequest.builder()
                .appointmentId(appointmentId)
                .paymentType(PaymentType.REMAINING)
                .paymentMethod(PaymentMethod.VNPAY)
                .build();

        PaymentUrlResponse response = paymentService.createPayment(request, customerIp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId) {

        PaymentResponse response = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/vnpay/callback")
    public ResponseEntity<PaymentResponse> vnpayCallback(
            @RequestParam Map<String, String> params,
            HttpServletRequest request) {
        log.info("Raw callback params: {}", params);
        PaymentCallbackRequest callbackRequest = PaymentCallbackRequest.builder()
                .parameters(params)
                .build();

        PaymentResponse response = paymentService.processPaymentCallback(callbackRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/vnpay/return")
    public ResponseEntity<Map<String, Object>> vnpayReturn(
            @RequestParam Map<String, String> params) {

        log.info("Received VNPay return with transaction: {}", params.get("vnp_TxnRef"));

        PaymentCallbackRequest callbackRequest = PaymentCallbackRequest.builder()
                .parameters(params)
                .build();

        PaymentResponse response = paymentService.processPaymentCallback(callbackRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Payment processed successfully");
        result.put("payment", response);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
           @PathVariable UUID paymentId) {

        log.info("Cancelling payment: {}", paymentId);
        PaymentResponse response = paymentService.cancelPayment(paymentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}/query")
    public ResponseEntity<PaymentResponse> queryPaymentStatus(
            @PathVariable UUID paymentId) {

        log.info("Received request to query payment status for payment ID: {}", paymentId);

        PaymentResponse response = paymentService.queryPaymentStatus(paymentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/transaction/{transactionId}/query")
    public ResponseEntity<PaymentResponse> queryPaymentStatusByTransactionId(
            @PathVariable String transactionId) {

        log.info("Received request to query payment status for transaction ID: {}", transactionId);

        PaymentResponse response = paymentService.queryPaymentStatus(transactionId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/process")
    public ResponseEntity<String> processPendingPayments() {

        log.info("Received request to process pending payments");

        paymentService.processProcessingPayments();

        return ResponseEntity.status(HttpStatus.OK).body("Processing completed");
    }

//    @PostMapping("/refund")
//    public ResponseEntity<PaymentRefundResponse> refundPayment(@RequestBody PaymentRefundRequest request) {
//        PaymentRefundResponse response = paymentService.refundPayment(request);
//        return ResponseEntity.ok(response);
//    }

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
            clientIp = request.getHeader("X-Forwarded");
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("Forwarded-For");
        }

        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getHeader("Forwarded");
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
