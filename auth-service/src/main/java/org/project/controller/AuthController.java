package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.project.dto.ApiResponse;
import org.project.dto.request.*;
import org.project.dto.response.*;
import org.project.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.EntityResponse;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Đăng nhập thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request);

        ApiResponse<TokenResponse> apiResponse = ApiResponse.<TokenResponse>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Làm mới token thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@RequestBody RegisterRequest request){
        authService.register(request);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message("Đăng ký thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody LogoutRequest request) {
        authService.logout(request);

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Đăng xuất thành công")
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<ApiResponse<VerifyTokenResponse>> verifyToken(@RequestBody VerifyTokenRequest request) {
        VerifyTokenResponse response = authService.verifyToken(request);

        ApiResponse<VerifyTokenResponse> apiResponse = ApiResponse.<VerifyTokenResponse>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Xác thực token thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<ForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        ForgotPasswordResponse response = authService.forgotPassword(request);

        ApiResponse<ForgotPasswordResponse> apiResponse = ApiResponse.<ForgotPasswordResponse>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Đã gửi email khôi phục mật khẩu")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<PasswordResetResponse>> resetPassword(
            @Valid @RequestBody PasswordResetRequest request) {

        PasswordResetResponse response = authService.resetPassword(request);

        ApiResponse<PasswordResetResponse> apiResponse = ApiResponse.<PasswordResetResponse>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("Đặt lại mật khẩu thành công")
                .data(response)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<String>> validateResetToken(@RequestParam String token) {
        boolean isValid = authService.validateResetToken(token);
        String message = isValid ? "Token hợp lệ" : "Token không hợp lệ hoặc đã hết hạn";

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .success(isValid)
                .code(isValid ? HttpStatus.OK.value() : HttpStatus.BAD_REQUEST.value())
                .message(message)
                .data(isValid ? "valid" : "invalid")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(isValid ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }
}
