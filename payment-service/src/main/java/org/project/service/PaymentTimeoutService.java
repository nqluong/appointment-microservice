package org.project.service;

public interface PaymentTimeoutService {
    
    /**
     * Xử lý các payment PENDING đã quá hạn (> 15 phút)
     * Publish PaymentFailedEvent để hủy appointment
     */
    void processExpiredPendingPayments();
}
