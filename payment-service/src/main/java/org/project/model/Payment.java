package org.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.project.enums.PaymentMethod;
import org.project.enums.PaymentStatus;
import org.project.enums.PaymentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(name = "appointment_id", nullable = false)
    UUID appointmentId;

    @Min(value = 0, message = "Amount must be non-negative")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    BigDecimal amount;

    @NotNull(message = "Payment type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    PaymentStatus paymentStatus;

    @Column(name = "transaction_id")
    String transactionId;

    @Column(name = "gateway_transaction_id")
    String gatewayTransactionId;

    @Column(name = "payment_url")
    String paymentUrl;

    @Column(name = "gateway_response", columnDefinition = "TEXT")
    String gatewayResponse;

    @Column(name = "notes")
    String notes;

    @Column(name = "payment_date")
    LocalDateTime paymentDate;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "refund_transaction_id")
    String refundTransactionId;

    @Column(name = "gateway_refund_id")
    String gatewayRefundId;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    String refundReason;

    @Column(name = "refund_date")
    LocalDateTime refundDate;

    @Column(name = "refund_gateway_response", columnDefinition = "TEXT")
    String refundGatewayResponse;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}
