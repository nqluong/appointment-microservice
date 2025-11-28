package org.project.consumer;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.project.exception.CustomException;
import org.project.exception.ErrorCode;
import org.project.model.EventErrorLog;
import org.project.repository.EventErrorLogRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;


@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaErrorHandler {

    KafkaTemplate<String, Object> kafkaTemplate;
    EventErrorLogRepository errorLogRepository;

    public void handleError(Object event, Exception e, String eventType) {
        log.error("Handling error for event type: {}", eventType, e);

        ErrorType errorType = categorizeError(e);

        switch (errorType) {
            case TRANSIENT:
                handleTransientError(event, e, eventType);
                break;
            case BUSINESS:
                handleBusinessError(event, e, eventType);
                break;
            case FATAL:
                handleFatalError(event, e, eventType);
                break;
        }
    }

    private ErrorType categorizeError(Exception e) {
        // Transient errors - worth retrying
        if (e instanceof DataAccessException ||
                e instanceof SocketTimeoutException) {
            return ErrorType.TRANSIENT;
        }

        if (e instanceof CustomException) {
            CustomException ce = (CustomException) e;
            if (ce.getErrorCode() == ErrorCode.APPOINTMENT_NOT_FOUND) {
                return ErrorType.BUSINESS;
            }
        }

        return ErrorType.FATAL;
    }

    private void handleTransientError(Object event, Exception e, String eventType) {
        log.warn("Transient error for {}, sending to retry topic", eventType);

        try {
            kafkaTemplate.send("appointment-retry", event);
        } catch (Exception retryError) {
            log.error("Failed to send to retry topic", retryError);
            sendToDLQ(event, e, eventType);
        }
    }

    private void handleBusinessError(Object event, Exception e, String eventType) {
        log.error("Business error for {}, logging and skipping", eventType);

        // Log to database for analysis
        persistErrorLog(event, e, eventType, "BUSINESS_ERROR");

        // Don't retry, but alert if needed
        if (shouldAlert(e)) {
            sendAlert(eventType, e.getMessage());
        }
    }

    public void handleFatalError(Object event, Exception e, String eventType) {
        log.error("FATAL error for {}, immediate action required", eventType);

        // Send to DLQ
        sendToDLQ(event, e, eventType);

        // Persist for investigation
        persistErrorLog(event, e, eventType, "FATAL_ERROR");

        // Alert immediately
        sendAlert(eventType, "FATAL: " + e.getMessage());
    }

    private void sendToDLQ(Object event, Exception e, String eventType) {
        try {
            kafkaTemplate.send("appointment-dlq", event);
            log.info("Sent {} to DLQ", eventType);
        } catch (Exception dlqError) {
            log.error("CRITICAL: Failed to send to DLQ", dlqError);
            // Last resort - at least log it
            persistErrorLog(event, dlqError, eventType, "DLQ_SEND_FAILED");
        }
    }

    private void persistErrorLog(Object event, Exception e, String eventType, String category) {
        try {
            EventErrorLog errorLog = EventErrorLog.builder()
                    .eventType(eventType)
                    .eventPayload(event.toString())
                    .errorMessage(e.getMessage())
                    .stackTrace(getStackTrace(e))
                    .category(category)
                    .createdAt(LocalDateTime.now())
                    .build();

            errorLogRepository.save(errorLog);
        } catch (Exception persistError) {
            log.error("Failed to persist error log", persistError);
        }
    }

    private boolean shouldAlert(Exception e) {
        // Define alerting criteria
        return e instanceof CustomException &&
                ((CustomException) e).getErrorCode() == ErrorCode.APPOINTMENT_NOT_FOUND;
    }

    private void sendAlert(String eventType, String message) {
        // Implement your alerting mechanism
        log.error("ALERT: EventType={}, Message={}", eventType, message);
        // Could send to Slack, email, PagerDuty, etc.
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private enum ErrorType {
        TRANSIENT,
        BUSINESS,
        FATAL
    }
}
