package org.project.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
public class AppointmentCodeGenerator {
    private static final String PREFIX = "AP";
    private static final int RANDOM_LENGTH = 4;

    private static final String SAFE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generatePublicCode() {
        StringBuilder code = new StringBuilder(PREFIX);

        long timestamp = System.currentTimeMillis();
        code.append(timestamp);

        for (int i = 0; i < RANDOM_LENGTH; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(SAFE_CHARS.length());
            code.append(SAFE_CHARS.charAt(randomIndex));
        }

        return code.toString();
    }

    public boolean isValidFormat(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }

        // Kiểm tra prefix
        if (!code.startsWith(PREFIX)) {
            return false;
        }

        if (code.length() < 19) {
            return false;
        }

        String timestampPart = code.substring(2, 15);
        try {
            Long.parseLong(timestampPart);
        } catch (NumberFormatException e) {
            return false;
        }

        // Kiểm tra phần random (4 ký tự cuối)
        String randomPart = code.substring(15);
        for (char c : randomPart.toCharArray()) {
            if (SAFE_CHARS.indexOf(c) == -1 && !Character.isDigit(c)) {
                return false;
            }
        }

        return true;
    }

    public String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim()
                .replaceAll("[\\s-]", "")
                .toUpperCase();
    }
}
