package org.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.project.dto.response.UserProfileResponse;
import org.project.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/user-profile")
@RequiredArgsConstructor
@Slf4j
public class InternalUserProfileController {
    private final ProfileService profileService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        UserProfileResponse userProfile = profileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }
}
