package org.project.service;

import org.project.dto.request.ForgotPasswordRequest;
import org.project.dto.request.PasswordResetRequest;
import org.project.dto.response.ForgotPasswordResponse;
import org.project.dto.response.PasswordResetResponse;

public interface PasswordResetService {

    ForgotPasswordResponse forgotPassword(ForgotPasswordRequest forgotPasswordRequest);

    PasswordResetResponse passwordReset(PasswordResetRequest passwordResetRequest);

    boolean validateResetToken(String token);

}
