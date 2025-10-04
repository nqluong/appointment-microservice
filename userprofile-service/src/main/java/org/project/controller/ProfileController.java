package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.project.common.security.token.GatewayUserPrincipal;
import org.project.common.security.util.SecurityUtils;
import org.project.dto.response.CompleteProfileResponse;
import org.project.service.PhotoUploadService;
import org.project.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/{userId}/profiles")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ProfileService profileService;
    private final PhotoUploadService photoUploadService;
    private final SecurityUtils securityUtils;

//    @PutMapping("/update")
//    //@RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
//    public ResponseEntity<CompleteProfileResponse> updateUserProfile(
//            @PathVariable UUID userId,
//            @Valid @RequestBody ProfileUpdateRequest request) {
//
//        CompleteProfileResponse response = profileService.updateProfile(userId, request);
//
//        return ResponseEntity.ok(response);
//    }

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

//    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//   // @RequireOwnershipOrAdmin(allowedRoles = {"DOCTOR", "PATIENT"})
//    public ResponseEntity<PhotoUploadResponse> uploadUserPhoto(
//            @PathVariable UUID userId,
//            @RequestParam("photo") MultipartFile photo) {
//
//
//        PhotoUploadRequest request = PhotoUploadRequest.builder()
//                .photo(photo)
//                .build();
//
//        PhotoUploadResponse response = photoUploadService.uploadUserPhoto(userId, request);
//
//        if (response.isSuccess()) {
//            return ResponseEntity.ok(response);
//        } else {
//            return ResponseEntity.badRequest().body(response);
//        }
//    }
}
