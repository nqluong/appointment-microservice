package org.project.config;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.query")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentQueryConfig {
    @Value("${payment.query.safety-days-before}")
    int safetyDaysBefore;

   @Value("${payment.query.min-minutes-before-query}")
    int minMinutesBeforeQuery;

    @Value("${payment.query.max-hours-for-query}")
    int maxHoursForQuery;

    @Value("${payment.query.query-enabled}")
    boolean queryEnabled;

    @Value("${payment.query.allow-old-payment-query}")
    boolean allowOldPaymentQuery ;
}
