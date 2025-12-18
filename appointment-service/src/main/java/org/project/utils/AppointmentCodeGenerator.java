package org.project.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Slf4j
@Component
public class AppointmentCodeGenerator {
    private static final String PREFIX = "AP";
    private static final int CODE_LENGTH = 8;

    private static final String SAFE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public String generatePublicCode() {
        StringBuilder code = new StringBuilder(PREFIX);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(SAFE_CHARS.length());
            code.append(SAFE_CHARS.charAt(randomIndex));
        }

        return code.toString();
    }

    public boolean isValidFormat(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }

        if (code.length() != 10) {
            return false;
        }

        // Kiểm tra prefix
        if (!code.startsWith(PREFIX)) {
            return false;
        }

        // Kiểm tra 8 ký tự sau prefix
        String codeBody = code.substring(2);
        for (char c : codeBody.toCharArray()) {
            if (SAFE_CHARS.indexOf(c) == -1) {
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
