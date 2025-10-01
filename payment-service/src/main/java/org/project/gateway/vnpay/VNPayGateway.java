package org.project.gateway.vnpay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.project.dto.request.PaymentCallbackRequest;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentStatus;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.gateway.PaymentGateway;
import org.project.gateway.dto.*;
import org.project.gateway.vnpay.config.VNPayConfig;
import org.project.gateway.vnpay.util.VNPayUtil;
import org.project.model.Payment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VNPayGateway implements PaymentGateway {
    VNPayConfig vnPayConfig;
    VNPayUtil vnPayUtil;
    RestTemplate restTemplate;
    ObjectMapper objectMapper;

    @Override
    public PaymentGatewayResponse createPaymentUrl(Payment payment, PaymentGatewayRequest request) {
        try {
            Map<String, String> params = buildPaymentParams(payment, request);
            String paymentUrl = vnPayUtil.buildPaymentUrl(vnPayConfig.getPaymentUrl(), params, vnPayConfig.getHashSecret());

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .paymentUrl(paymentUrl)
                    .transactionId(payment.getTransactionId())
                    .message("Payment URL created successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error creating VNPay payment URL for transaction: {}", payment.getTransactionId(), e);
            throw new CustomException(ErrorCode.PAYMENT_GATEWAY_ERROR, e.getMessage());
        }
    }

    @Override
    public PaymentVerificationResult verifyPayment(PaymentCallbackRequest callbackRequest) {
        try {
            Map<String, String> params = callbackRequest.getParameters();

            if (!vnPayUtil.verifyCallback(params, vnPayConfig.getHashSecret())) {
                log.error("VNPay callback signature verification failed");
                return PaymentVerificationResult.builder()
                        .valid(false)
                        .message("Invalid signature")
                        .build();
            }

            return buildVerificationResult(params);
        } catch (Exception e) {
            log.error("Error verifying VNPay payment", e);
            throw new CustomException(ErrorCode.VNPAY_SIGNATURE_VERIFICATION_FAILED, e);
        }
    }

    @Override
    public PaymentQueryResult queryPaymentStatus(String transactionId, String transactionDate) {
        try {
            log.info("Querying VNPay payment status for transaction: {}", transactionId);

            Map<String, Object> requestBody = buildQueryRequest(transactionId, transactionDate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    vnPayConfig.getQueryUrl(),
                    entity,
                    String.class
            );

            return parseQueryResponse(response.getBody(), transactionId);

        } catch (Exception e) {
            log.error("Error querying VNPay payment status for transaction: {}", transactionId, e);
            return PaymentQueryResult.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message(e.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentRefundResult refundPayment(RefundRequest refundRequest) {
        try {
            validateRefundRequest(refundRequest);
            Map<String, Object> requestBody = buildRefundRequest(refundRequest);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    vnPayConfig.getRefundUrl(),
                    entity,
                    String.class
            );
            return parseRefundResponse(response.getBody(),refundRequest);

        }catch (Exception e) {
            return PaymentRefundResult.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .refundTransactionId(refundRequest.getRefundTransactionId())
                    .message("Refund failed: " + e.getMessage())
                    .errorCode("REFUND_ERROR")
                    .build();
        }
    }

    @Override
    public boolean supports(PaymentMethod paymentMethod) {
        return PaymentMethod.VNPAY.equals(paymentMethod);
    }

    private Map<String, String> buildPaymentParams(Payment payment, PaymentGatewayRequest request) {
        long amount = payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        String createDate = vnPayUtil.getCurrentDateTime();

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", vnPayConfig.getVersion());
        params.put("vnp_Command", vnPayConfig.getCommand());
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amount));
        params.put("vnp_CurrCode", vnPayConfig.getCurrCode());
        params.put("vnp_TxnRef", payment.getTransactionId());
        params.put("vnp_OrderInfo", request.getOrderInfo());
        params.put("vnp_OrderType", vnPayConfig.getOrderType());
        params.put("vnp_Locale", vnPayConfig.getLocale());
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", request.getCustomerIp());
        params.put("vnp_CreateDate", createDate);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        calendar.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(calendar.getTime());
        params.put("vnp_ExpireDate", vnp_ExpireDate);

        return params;
    }

    private PaymentVerificationResult buildVerificationResult(Map<String, String> params) {
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountStr = params.get("vnp_Amount");

        PaymentStatus status = "00".equals(responseCode) ?
                PaymentStatus.COMPLETED : PaymentStatus.FAILED;

        BigDecimal amount = amountStr != null ?
                new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : null;

        return PaymentVerificationResult.builder()
                .valid(true)
                .transactionId(txnRef)
                .gatewayTransactionId(transactionNo)
                .amount(amount)
                .status(status)
                .message(getResponseMessage(responseCode))
                .responseData(params.toString())
                .build();
    }

    private Map<String, Object> buildQueryRequest(String transactionId, String transactionDate) {
        String requestId = vnPayUtil.generateRequestId();
        String createDate = vnPayUtil.getCurrentDateTime();
        String orderInfo = "Query transaction: " + transactionId;

        // Use provided transactionDate or extract from transactionId
        String txnDate = (transactionDate != null) ? transactionDate :
                vnPayUtil.extractTransactionDate(transactionId);

        String secureHash = vnPayUtil.createQuerySecureHash(
                requestId,
                vnPayConfig.getVersion(),
                "querydr",
                vnPayConfig.getTmnCode(),
                transactionId,
                txnDate,
                createDate,
                "127.0.0.1",
                orderInfo,
                vnPayConfig.getHashSecret()
        );

        Map<String, Object> request = new HashMap<>();
        request.put("vnp_RequestId", requestId);
        request.put("vnp_Version", vnPayConfig.getVersion());
        request.put("vnp_Command", "querydr");
        request.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        request.put("vnp_TxnRef", transactionId);
        request.put("vnp_OrderInfo", orderInfo);
        request.put("vnp_TransactionDate", txnDate);
        request.put("vnp_CreateDate", createDate);
        request.put("vnp_IpAddr", "127.0.0.1");
        request.put("vnp_SecureHash", secureHash);

        return request;
    }

    private PaymentQueryResult parseQueryResponse(String responseBody, String transactionId) {
        try {

            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);

            String responseCode = (String) response.get("vnp_ResponseCode");
            String transactionStatus = (String) response.get("vnp_TransactionStatus");
            String vnpTransactionNo = (String) response.get("vnp_TransactionNo");
            String amountStr = (String) response.get("vnp_Amount");
            String payDate = (String) response.get("vnp_PayDate");

            if (!"00".equals(responseCode)) {
                return PaymentQueryResult.builder()
                        .success(false)
                        .status(PaymentStatus.FAILED)
                        .transactionId(transactionId)
                        .message("Query failed with response code: " + responseCode)
                        .rawResponse(responseBody)
                        .build();
            }

            PaymentStatus status = determinePaymentStatus(transactionStatus);
            BigDecimal amount = amountStr != null ?
                    new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : null;

            LocalDateTime paymentDate = parsePaymentDate(payDate);

            return PaymentQueryResult.builder()
                    .success(true)
                    .status(status)
                    .transactionId(transactionId)
                    .gatewayTransactionId(vnpTransactionNo)
                    .amount(amount)
                    .responseCode(transactionStatus)
                    .paymentDate(paymentDate)
                    .message(getTransactionStatusMessage(transactionStatus))
                    .rawResponse(responseBody)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing VNPay query response", e);
            return PaymentQueryResult.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .message("Error parsing VNPay query response: " + e.getMessage())
                    .build();

        }
    }


    private PaymentStatus determinePaymentStatus(String transactionStatus) {
        if (transactionStatus == null) {
            return PaymentStatus.FAILED;
        }

        switch (transactionStatus) {
            case "00": return PaymentStatus.COMPLETED;
            case "01": return PaymentStatus.PENDING;
            default: return PaymentStatus.FAILED;
        }
    }

    private LocalDateTime parsePaymentDate(String payDate) {
        if (payDate == null || payDate.isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(payDate, formatter);
        } catch (Exception e) {
            log.warn("Error parsing payment date: {}", payDate, e);
            return null;
        }
    }

    private void validateRefundRequest(RefundRequest request) {
        if (request.getOriginalTransactionId() == null || request.getOriginalTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Original transaction ID is required");
        }
        if (request.getRefundTransactionId() == null || request.getRefundTransactionId().trim().isEmpty()) {
            throw new IllegalArgumentException("Refund transaction ID is required");
        }
        if (request.getRefundAmount() == null || request.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than 0");
        }
        if (request.getOriginalAmount() != null &&
                request.getRefundAmount().compareTo(request.getOriginalAmount()) > 0) {
            throw new IllegalArgumentException("Refund amount cannot be greater than original amount");
        }
    }

    private Map<String, Object> buildRefundRequest(RefundRequest refundRequest) {
        String requestId = vnPayUtil.generateRequestId();
        String createDate = vnPayUtil.getCurrentDateTime();

        long refundAmount = refundRequest.getRefundAmount().multiply(BigDecimal.valueOf(100)).longValue();

        String transactionType = determineTransactionType(refundRequest);
        String orderInfo = refundRequest.getOrderInfo() != null ?
                refundRequest.getOrderInfo() :
                "Refund for transaction: " + refundRequest.getOriginalTransactionId();

        String txnDate = (refundRequest.getTransactionDate() != null) ?
                refundRequest.getTransactionDate() :
                vnPayUtil.extractTransactionDate(refundRequest.getOriginalTransactionId());

        String secureHash = vnPayUtil.createRefundSecureHash(
                requestId,
                vnPayConfig.getVersion(),
                "refund",
                vnPayConfig.getTmnCode(),
                transactionType,
                refundRequest.getOriginalTransactionId(),
                String.valueOf(refundAmount),
                refundRequest.getGatewayTransactionId(),
                txnDate,
                "ADMIN",
                refundRequest.getCustomerIp() != null ? refundRequest.getCustomerIp() : "127.0.0.1",
                createDate,
                orderInfo,
                vnPayConfig.getHashSecret()
        );

        Map<String, Object> request = new HashMap<>();
        request.put("vnp_RequestId", requestId);
        request.put("vnp_Version", vnPayConfig.getVersion());
        request.put("vnp_Command", "refund");
        request.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        request.put("vnp_TransactionType", transactionType);
        request.put("vnp_TxnRef", refundRequest.getOriginalTransactionId());
        request.put("vnp_Amount", refundAmount);
        request.put("vnp_OrderInfo", orderInfo);
        request.put("vnp_TransactionNo", refundRequest.getGatewayTransactionId());
        request.put("vnp_TransactionDate", txnDate);
        request.put("vnp_CreateBy", "ADMIN");
        request.put("vnp_CreateDate", createDate);
        request.put("vnp_IpAddr", refundRequest.getCustomerIp() != null ? refundRequest.getCustomerIp() : "127.0.0.1");
        request.put("vnp_SecureHash", secureHash);
        return request;
    }

    private String determineTransactionType(RefundRequest refundRequest) {
        // 02: Hoàn trả toàn phần
        // 03: Hoàn trả một phần
        if (refundRequest.getOriginalAmount() != null) {
            return refundRequest.getRefundAmount().compareTo(refundRequest.getOriginalAmount()) == 0 ? "02" : "03";
        }
        return "03"; // Default to partial refund
    }

    private PaymentRefundResult parseRefundResponse(String responseBody, RefundRequest refundRequest) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            String responseCode = (String) response.get("vnp_ResponseCode");
            String message = (String) response.get("vnp_Message");
            String vnpTransactionNo = (String) response.get("vnp_TransactionNo");
            String amountStr = (String) response.get("vnp_Amount");
            String bankTransactionNo = (String) response.get("vnp_BankTransactionNo");

            boolean success = "00".equals(responseCode);
            PaymentStatus status = success ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;

            BigDecimal refundAmount = amountStr != null ?
                    new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : null;

            return PaymentRefundResult.builder()
                    .success(success)
                    .refundTransactionId(refundRequest.getRefundTransactionId())
                    .gatewayRefundId(vnpTransactionNo != null ? vnpTransactionNo : bankTransactionNo)
                    .refundAmount(refundAmount)
                    .status(status)
                    .responseCode(responseCode)
                    .message(success ? "Refund processed successfully" :
                            (message != null ? message : getRefundResponseMessage(responseCode)))
                    .refundDate(LocalDateTime.now())
                    .rawResponse(responseBody)
                    .errorCode(success ? null : responseCode)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing VNPay refund response", e);
            return PaymentRefundResult.builder()
                    .success(false)
                    .status(PaymentStatus.FAILED)
                    .refundTransactionId(refundRequest.getRefundTransactionId())
                    .message("Error parsing refund response: " + e.getMessage())
                    .errorCode("PARSE_ERROR")
                    .build();
        }
    }

    private String getRefundResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Giao dịch hoàn tiền thành công";
            case "02": return "Merchant không hợp lệ";
            case "03": return "Dữ liệu gửi sang không đúng định dạng";
            case "04": return "Không cho phép hoàn tiền";
            case "13": return "Chỉ cho phép hoàn tiền một phần";
            case "91": return "Không tìm thấy giao dịch yêu cầu hoàn tiền";
            case "93": return "Số tiền hoàn tiền không hợp lệ";
            case "94": return "Giao dịch đã được hoàn tiền trước đó";
            case "95": return "Giao dịch không thành công";
            case "97": return "Chữ ký không hợp lệ";
            default: return "Giao dịch hoàn tiền thất bại";
        }
    }

    private String getTransactionStatusMessage(String status) {
        switch (status) {
            case "00": return "Giao dịch thanh toán được thực hiện thành công";
            case "01": return "Giao dịch chưa hoàn tất";
            case "02": return "Giao dịch bị lỗi";
            case "04": return "Giao dịch đảo (Khách hàng đã bị trừ tiền tại Ngân hàng nhưng GD chưa thành công ở VNPAY)";
            case "05": return "VNPAY đang xử lý giao dịch này (GD hoàn tiền)";
            case "06": return "VNPAY đã gửi yêu cầu hoàn tiền sang Ngân hàng (GD hoàn tiền)";
            case "07": return "Giao dịch bị nghi ngờ";
            case "09": return "GD Hoàn trả bị từ chối";
            default: return "Trạng thái không xác định";
        }
    }

    private String getResponseMessage(String responseCode) {
        switch (responseCode) {
            case "00": return "Transaction successful";
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ";
            case "09": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng";
            case "10": return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11": return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12": return "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa";
            case "13": return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch";
            case "24": return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51": return "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch";
            case "65": return "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày";
            case "75": return "Ngân hàng thanh toán đang bảo trì";
            case "79": return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            default: return "Giao dịch thất bại";
        }
    }
}
