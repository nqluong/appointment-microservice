package org.project.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpirationResult {

    boolean appointmentCancelled = false;

    int paymentsCancelled = 0;

    boolean slotReleased = false;

    public static ExpirationResult create() {
        return new ExpirationResult();
    }

    public boolean isFullySuccessful() {
        return appointmentCancelled && slotReleased;
    }

    public boolean hasFailures() {
        return !isFullySuccessful();
    }

    @Override
    public String toString() {
        return String.format("appointment=%s, payments=%d, slot=%s",
                appointmentCancelled, paymentsCancelled, slotReleased);
    }
}
