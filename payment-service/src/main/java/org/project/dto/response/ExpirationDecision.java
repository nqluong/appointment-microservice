package org.project.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import org.project.enums.ExpirationAction;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExpirationDecision {
    ExpirationAction action;
    long remainingGraceMinutes;

    // Tạo decision hủy appointment ngay lập tức
    public static ExpirationDecision cancelImmediately() {
        return new ExpirationDecision(ExpirationAction.CANCEL_IMMEDIATELY, 0);
    }

    // Tạo decision tạm hoãn hủy appointment
    public static ExpirationDecision defer(long remainingMinutes) {
        return new ExpirationDecision(ExpirationAction.DEFER, remainingMinutes);
    }

    // Tạo decision buộc phải hủy appointment
    public static ExpirationDecision forceCancel() {
        return new ExpirationDecision(ExpirationAction.FORCE_CANCEL, 0);
    }
}
