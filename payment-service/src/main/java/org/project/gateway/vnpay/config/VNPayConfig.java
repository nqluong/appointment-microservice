package org.project.gateway.vnpay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConfigurationProperties(prefix = "vnpay")
public class VNPayConfig {
    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    @Value("${vnpay.currCode}")
    private String currCode;

    @Value("${vnpay.locale}")
    private String locale;

    @Value("${vnpay.paymentUrl}")
    private String paymentUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    @Value("${vnpay.orderType}")
    private String orderType;

    @Value("${vnpay.query-url}")
    private String queryUrl;

    @Value("${vnpay.refundUrl}")
    private String refundUrl;
}
