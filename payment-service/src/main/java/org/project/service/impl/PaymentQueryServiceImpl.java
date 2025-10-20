package org.project.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.project.config.PaymentQueryConfig;
import org.project.dto.response.PaymentResponse;
import org.project.enums.PaymentStatus;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.gateway.PaymentGateway;
import org.project.gateway.PaymentGatewayFactory;
import org.project.gateway.dto.PaymentQueryResult;
import org.project.mapper.PaymentMapper;
import org.project.model.Payment;
import org.project.repository.PaymentRepository;
import org.project.service.PaymentQueryService;
import org.project.service.PaymentStatusHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentQueryServiceImpl implements PaymentQueryService {

    PaymentRepository paymentRepository;
    PaymentGatewayFactory paymentGatewayFactory;
    PaymentMapper paymentMapper;
    PaymentQueryConfig paymentQueryConfig;
    PaymentStatusHandler paymentStatusHandler;

    @Override
    public PaymentResponse queryPaymentStatus(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!isPaymentSafeToQuery(payment)) {
            log.warn("Giao dịch {} đã quá cũ để truy vấn an toàn, được tạo lúc: {}",
                    paymentId, payment.getCreatedAt());
            return paymentMapper.toResponse(payment);
        }

        if (!PaymentStatus.PENDING.equals(payment.getPaymentStatus())
                && !PaymentStatus.PROCESSING.equals(payment.getPaymentStatus())) {
            return paymentMapper.toResponse(payment);
        }

        try {
            PaymentGateway paymentGateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());
            PaymentQueryResult queryResult = paymentGateway.queryPaymentStatus(
                    payment.getTransactionId(),
                    formatTransactionDate(payment.getCreatedAt())
            );
            return processQueryResult(payment, queryResult);
        } catch (Exception e) {
            log.error("Lỗi khi truy vấn trạng thái giao dịch: {}", paymentId, e);
            throw new CustomException(ErrorCode.PAYMENT_QUERY_FAILED);
        }
    }

    @Override
    public PaymentResponse queryPaymentStatus(String transactionId) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));
        return queryPaymentStatus(payment.getId());
    }

    @Override
    public void processProcessingPayments() {
        if (!paymentQueryConfig.isQueryEnabled()) {
            log.info("Truy vấn giao dịch đã bị vô hiệu hóa, bỏ qua xử lý các giao dịch đang chờ");
            return;
        }

        List<Payment> processingPayments = getProcessingPayments();
        processPayments(processingPayments);
    }

    private List<Payment> getProcessingPayments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoffTime = now.minusMinutes(paymentQueryConfig.getMinMinutesBeforeQuery());
        LocalDateTime safetyDate = now.minusDays(paymentQueryConfig.getSafetyDaysBefore());

        if (paymentQueryConfig.isAllowOldPaymentQuery()) {
            return paymentRepository.findByStatusAndCreatedAtBeforeWithSafety(
                    PaymentStatus.PROCESSING, cutoffTime, LocalDateTime.of(2020, 1, 1, 0, 0));
        } else {
            return paymentRepository.findByStatusAndCreatedAtBeforeWithSafety(
                    PaymentStatus.PROCESSING, cutoffTime, safetyDate);
        }
    }

    private void processPayments(List<Payment> payments) {
        int processedCount = 0;
        int errorCount = 0;

        for (Payment payment : payments) {
            try {
                if (!isPaymentSafeToQuery(payment)) {
                    continue;
                }

                PaymentGateway gateway = paymentGatewayFactory.getGateway(payment.getPaymentMethod());
                PaymentQueryResult queryResult = gateway.queryPaymentStatus(
                        payment.getTransactionId(),
                        formatTransactionDate(payment.getCreatedAt())
                );

                processQueryResult(payment, queryResult);
                processedCount++;
                Thread.sleep(1000);

            } catch (Exception e) {
                log.error("Lỗi khi xử lý giao dịch đang chờ: {}", payment.getTransactionId(), e);
                errorCount++;
            }
        }

        log.info("Đã hoàn thành xử lý các giao dịch đang chờ. Đã xử lý: {}, Lỗi: {}, Tổng số: {}",
                processedCount, errorCount, payments.size());
    }

    private boolean isPaymentSafeToQuery(Payment payment) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime paymentCreatedAt = payment.getCreatedAt();

        LocalDateTime safetyDate = now.minusDays(paymentQueryConfig.getSafetyDaysBefore());
        if (!paymentQueryConfig.isAllowOldPaymentQuery() && paymentCreatedAt.isBefore(safetyDate)) {
            return false;
        }

        LocalDateTime minQueryTime = now.minusMinutes(paymentQueryConfig.getMinMinutesBeforeQuery());
        if (paymentCreatedAt.isAfter(minQueryTime)) {
            return false;
        }

        LocalDateTime maxQueryTime = now.minusHours(paymentQueryConfig.getMaxHoursForQuery());
        if (paymentCreatedAt.isBefore(maxQueryTime)) {
            return false;
        }

        return true;
    }

    private PaymentResponse processQueryResult(Payment payment, PaymentQueryResult queryResult) {
        if (!queryResult.isSuccess()) {
            log.warn("Truy vấn giao dịch thất bại cho mã giao dịch: {}, thông báo: {}",
                    payment.getTransactionId(), queryResult.getMessage());
            return paymentMapper.toResponse(payment);
        }

        PaymentStatus newStatus = queryResult.getStatus();
        boolean statusChanged = !payment.getPaymentStatus().equals(newStatus);

        if (statusChanged) {
            updatePaymentFromQueryResult(payment, queryResult, newStatus);
            handleStatusChange(payment, newStatus);
        }

        return paymentMapper.toResponse(payment);
    }

    private void updatePaymentFromQueryResult(Payment payment, PaymentQueryResult queryResult, PaymentStatus newStatus) {
        payment.setPaymentStatus(newStatus);
        payment.setUpdatedAt(LocalDateTime.now());

        if (queryResult.getGatewayTransactionId() != null) {
            payment.setGatewayTransactionId(queryResult.getGatewayTransactionId());
        }

        if (queryResult.getPaymentDate() != null) {
            payment.setPaymentDate(queryResult.getPaymentDate());
        }

        if (queryResult.getRawResponse() != null) {
            payment.setGatewayResponse(queryResult.getRawResponse());
        }

        paymentRepository.save(payment);
    }

    private void handleStatusChange(Payment payment, PaymentStatus newStatus) {
        switch (newStatus) {
            case COMPLETED:
                paymentStatusHandler.handlePaymentSuccess(payment.getId());
                break;
            case FAILED:
                paymentStatusHandler.handlePaymentFailure(payment.getId());
                break;
            case PENDING:
                break;
        }
    }

    private String formatTransactionDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return dateTime.format(formatter);
    }
}
