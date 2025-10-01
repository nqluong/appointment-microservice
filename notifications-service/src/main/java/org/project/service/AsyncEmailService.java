package org.project.service;

public interface AsyncEmailService {
    void sendPasswordResetEmailAsync(
            String email,
            String userName,
            String resetToken,
            String resetUrl,
            int expiryTime);
}
