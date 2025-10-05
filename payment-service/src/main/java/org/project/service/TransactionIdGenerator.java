package org.project.service;

public interface TransactionIdGenerator {

     // Tạo ID cho xử lý thanh toán
    String generateTransactionId();

    //Tạo ID cho xử lý hoàn tiền
    String generateRefundTransactionId();
}
