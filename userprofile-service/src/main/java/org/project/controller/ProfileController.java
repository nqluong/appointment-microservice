package org.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.common.security.annotation.RequireOwnershipOrAdmin;
import org.project.common.security.token.GatewayUserPrincipal;
import org.project.common.security.util.SecurityUtils;
import org.project.dto.ApiResponse;
import org.project.dto.ErrorResponse;
import org.project.dto.request.ProfileUpdateRequest;
import org.project.dto.response.CompleteProfileResponse;
import org.project.service.FileStorageService;
import org.project.service.ProfileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    private final FileStorageService fileStorageService;
    private final SecurityUtils securityUtils;

    @PutMapping("/update")
    @RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
    public ResponseEntity<CompleteProfileResponse> updateUserProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody ProfileUpdateRequest request) {

        CompleteProfileResponse response = profileService.updateProfile(userId, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<CompleteProfileResponse> getMyProfile(
            @AuthenticationPrincipal GatewayUserPrincipal principal) {

        CompleteProfileResponse response = profileService.getCompleteProfile(principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/complete")
    public ResponseEntity<CompleteProfileResponse> getCompleteProfile(
            @PathVariable UUID userId) {
        securityUtils.validateUserAccess(userId);
        CompleteProfileResponse response = profileService.getCompleteProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
    public ResponseEntity<?> uploadUserPhoto(
            @PathVariable UUID userId,
            @RequestParam("photo") MultipartFile photo) {

        try {
            if (photo == null || photo.isEmpty()) {
                log.error("Tệp ảnh trống được gửi lên cho user {}", userId);
                return ResponseEntity.badRequest().body(
                    ErrorResponse.builder()
                        .success(false)
                        .code(400)
                        .message("Tệp ảnh không được để trống")
                        .timestamp(LocalDateTime.now())
                        .build()
                );
            }

            String avatarUrl = fileStorageService.uploadUserPhoto(photo, userId);

            Map<String, String> data = Map.of("avatarUrl", avatarUrl);
            
            ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                    .success(true)
                    .code(200)
                    .message("Tải lên ảnh đại diện thành công")
                    .data(data)
                    .timestamp(LocalDateTime.now())
                    .build();
                    
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Lỗi khi tải lên ảnh cho user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                    .success(false)
                    .code(500)
                    .message("Không thể tải lên ảnh đại diện")
                    .details(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build()
            );
        }
    }
}
