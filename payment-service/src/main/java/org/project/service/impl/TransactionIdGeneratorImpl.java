package org.project.service.impl;


import org.project.service.TransactionIdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TransactionIdGeneratorImpl implements TransactionIdGenerator {

    private static final String PAYMENT_PREFIX = "TXN";
    private static final String REFUND_PREFIX = "RFD";
    private static final int UUID_SUBSTRING_LENGTH = 8;

    @Override
    public String generateTransactionId() {
        return generateId(PAYMENT_PREFIX);
    }

    @Override
    public String generateRefundTransactionId() {
        return generateId(REFUND_PREFIX);
    }

    private String generateId(String prefix) {
        long timestamp = System.currentTimeMillis();
        String uuidSuffix = UUID.randomUUID()
                .toString()
                .substring(0, UUID_SUBSTRING_LENGTH)
                .toUpperCase();

        return prefix + timestamp + uuidSuffix;
    }
}
